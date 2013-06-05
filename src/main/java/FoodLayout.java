import com.google.common.collect.ImmutableList;
import org.joda.time.DateMidnight;

import java.util.List;

import static com.google.common.collect.ImmutableList.of;

/**
 * @author dmitry.mamonov
 *         Created: 6/4/13 7:11 PM
 */
public class FoodLayout {
    public static void main(String[] args) {
        final Поход осетия2013 = new Поход("Осетия Июль/Август 2013", of(
                new Переход("Первая часть", of(
                        День.поезд("старт. г. Воронеж, Ж/Д ст Придача, отправление в 7.55"),
                        День.запасной("9.20 прибытие во Владикавказ, регистрация в МЧС, развоз забросок, пос. Дзинага, отходим от поселка на ночевки"),
                        День.ход("р. Бартуй, подход под перевал Красивый"),
                        День.штурм("пер. Красивый (1Б, 3606, №39), спуск в ущелье Фастагдон"),
                        День.ход("подход под перевал Воологата"),
                        День.штурм("перевал Воологата (2А, 4058 ,№11)"),
                        День.запасной("запасной день, спуск вниз по леднику Сонгути, выход на зеленку (2А, 3700, №165???)"),
                        День.днёвка("Дневка по ситуации"),
                        День.ход("подход под перевал Родина северная"),
                        День.штурм("перевал Родина северная (2А, 3989, №178), спуск к спасателям, забор заброски (5к.с. + 3 к.с.)")
                )),
                new Переход("Вторая часть", of(
                        День.ход("подъем на ледник Цея к хижине"),
                        День.днёвка("хижина, дневка, встреча"),
                        День.ход("подход под перевал Ронкетти, !обход ледопада"),
                        День.штурм("перевал Ронкетти (2Б*, 3774, №148)"),
                        День.штурм("перевал Заромаг Южный (2А, 3761, №151), спуск на зеленые ночевки"),
                        День.ход("Спуск в поселок Нижний Заромаг, объезд озера на авто, подъем в ущелье Льядон (Нар), встреча с 2 к.с., подъезд в поселок Кармадон, подъем до нижних мин. Источников.")
                )),
                new Переход("Третья часть", of(
                        День.ход("подъем до верхних мин. Источников."),
                        День.ход("подъем в базовый лагерь на склоны ОЖД"),
                        День.штурм("Восхождение на Казбек (2Б, 5034)"),
                        День.днёвка("запасной день на восхождение"),
                        День.днёвка("запасной день на восхождение"),
                        День.ход("Спуск до нижних мин. Источников, поселок Кармадон, отъезд во Владикавказ"),
                        День.поезд("поезд, Воронеж")
                ))
        ));
    }
}

abstract class Aggregatable {
    private final String name;

    protected Aggregatable(String name) {
        this.name = name;
    }

    String getName() {
        return this.name;
    }

    abstract int getWeight();

    abstract float getAmount();

    protected static String callerName() {
        return new Exception().getStackTrace()[1].getMethodName();
    }
}

class AggregatableList<T extends Aggregatable> extends Aggregatable {
    private final List<T> entries;

    protected AggregatableList(final String name, final List<T> entries) {
        super(name);
        this.entries = entries;
    }

    List<T> getEntries() {
        return entries;
    }

    @Override
    int getWeight() {
        int sum = 0;
        for (T entry : entries) {
            sum += entry.getWeight();
        }
        return sum;
    }

    @Override
    float getAmount() {
        float sum = 0.0f;
        for (T entry : entries) {
            sum += entry.getAmount();
        }
        return sum;
    }
}

class Поход extends AggregatableList<Переход> {
    public Поход(String name, List<Переход> entries) {
        super(name, entries);
    }

}

class Переход extends AggregatableList<День> {
    public Переход(String name, List<День> entries) {
        super(name, entries);
    }
}

class День extends AggregatableList<ПриёмПищи> {
    private static DateMidnight currentDay = new DateMidnight(2013, 7, 13);

    private static String dateToString(DateMidnight day) {
        final int month = day.getMonthOfYear();
        return day.getDayOfMonth() + " " + (month == 7 ? "Июль" : (month == 8 ? "Август" : month));
    }

    private День(String name, List<ПриёмПищи> entries) {
        super(dateToString(currentDay) + ": " + name, entries);
        currentDay = currentDay.plusDays(1);
    }

    static День штурм(String comment, ПриёмПищи... entries) {
        return new День(callerName() + ", " + comment, ImmutableList.<ПриёмПищи>builder()
                .add(entries)
                .add(ПриёмПищи.карманное_питание())
                .build());
    }

    static День ход(String comment, ПриёмПищи... entries) {
        return new День(callerName() + ", " + comment, ImmutableList.<ПриёмПищи>builder()
                .add(entries)
                .add(ПриёмПищи.карманное_питание())
                .build());
    }


    static День днёвка(String comment, ПриёмПищи... entries) {
        return new День(callerName() + ", " + comment, ImmutableList.<ПриёмПищи>builder()
                .add(entries)
                .add(ПриёмПищи.без_карманки())
                .build());
    }

    static День запасной(String comment, ПриёмПищи... entries) {
        return new День(callerName() + ", " + comment, ImmutableList.<ПриёмПищи>builder()
                .add(entries)
                .add(ПриёмПищи.без_карманки())
                .build());
    }

    static День поезд(String comment) {
        return new День(callerName() + ", " + comment, ImmutableList.<ПриёмПищи>builder()
                .add(ПриёмПищи.питание_по_личному_успотрению())
                .build());
    }
}

class ПриёмПищи extends AggregatableList<Продукт> {
    private ПриёмПищи(final String name, final List<Продукт> foods) {
        super(name, foods);
    }

    static ПриёмПищи завтрак(Продукт... foods) {
        return new ПриёмПищи(callerName(), ImmutableList.<Продукт>builder()
                .add(Продукт.чай(), Продукт.сахар(), Продукт.печенье())
                .build());
    }

    static ПриёмПищи обед(Продукт... foods) {
        return new ПриёмПищи(callerName(), ImmutableList.<Продукт>builder().build());
    }

    static ПриёмПищи ужин(Продукт... foods) {
        return new ПриёмПищи(callerName(), ImmutableList.<Продукт>builder().build());
    }

    static ПриёмПищи перекус(Продукт... foods) {
        return new ПриёмПищи(callerName(), ImmutableList.<Продукт>builder().build());
    }

    static List<Продукт> общиеПродукты() {
        return of(Продукт.чай());
    }

    static ПриёмПищи без_карманки() {
        return new ПриёмПищи(callerName(), ImmutableList.<Продукт>builder().build());
    }

    static ПриёмПищи питание_по_личному_успотрению() {
        return new ПриёмПищи(callerName(), ImmutableList.<Продукт>builder().build());
    }


    static ПриёмПищи карманное_питание() {
        return new ПриёмПищи(callerName(),
                ImmutableList.<Продукт>builder().add(
                        Продукт.грецкий(),
                        Продукт.миндаль(),
                        Продукт.кешью(),
                        Продукт.курага(),
                        Продукт.чернослив(),
                        Продукт.карамель()
                ).build());
    }

}

class Продукт extends Aggregatable {
    private final int weight;
    private final float amount;

    Продукт(final String name, final int weight, final float amount) {
        super(name);
        this.weight = weight;
        this.amount = amount;
    }

    Продукт(final String name, final int weight) {
        this(name, weight, 1.0f);
    }

    @Override
    int getWeight() {
        return weight;
    }

    @Override
    float getAmount() {
        return amount;
    }

    static Продукт чай() {
        return new Продукт(callerName(), 2); //OK
    }

    static Продукт сахар() {
        return new Продукт(callerName(), 15); //OK
    }

    static Продукт печенье() {
        return new Продукт(callerName(), 20); //OK
    }

    static Продукт сухари() {
        return new Продукт(callerName(), 15); //OK
    }

    static Продукт сникерс() {
        return new Продукт(callerName(), 55); //OK
    }

    static Продукт сыр_в_перекус() {
        return new Продукт(callerName(), 30); //OK
    }


    static Продукт сало() {
        return new Продукт(callerName(), 30); //OK
    }

    static Продукт сыр() {
        return new Продукт(callerName(), 30); //OK
    }

    static Продукт грецкий() {
        return new Продукт(callerName(), 7); //OK
    }

    static Продукт карамель() {
        return new Продукт(callerName(), 30); //OK
    }

    static Продукт кешью() {
        return new Продукт(callerName(), 7); //OK
    }

    static Продукт курага() {
        return new Продукт(callerName(), 22); //OK
    }

    static Продукт миндаль() {
        return new Продукт(callerName(), 7); //OK
    }

    static Продукт чернослив() {
        return new Продукт(callerName(), 22); //OK
    }
}