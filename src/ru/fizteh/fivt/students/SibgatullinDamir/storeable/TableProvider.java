package ru.fizteh.fivt.students.SibgatullinDamir.storeable;


import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * Управляющий класс для работы с {@link storeable.Table таблицами}
 *
 * Предполагает, что актуальная версия с устройства хранения, сохраняется при создании
 * экземпляра объекта. Далее ввод-вывод выполняется только в момент создания и удаления
 * таблиц.
 *
 * Данный интерфейс не является потокобезопасным.
 */
public interface TableProvider {

    /**
     * Возвращает таблицу с указанным названием.
     *
     * Последовательные вызовы метода с одинаковыми аргументами должны возвращать один и тот же объект таблицы,
     * если он не был удален с помощью {@link #removeTable(String)}.
     *
     * @param name Название таблицы.
     * @return Объект, представляющий таблицу. Если таблицы с указанным именем не существует, возвращает null.
     *
     * @throws IllegalArgumentException Если название таблицы null или имеет недопустимое значение.
     */
    Table getTable(String name);

    /**
     * Создаёт таблицу с указанным названием.
     * Создает новую таблицу. Совершает необходимые дисковые операции.
     *
     * @param name Название таблицы.
     * @param columnTypes Типы колонок таблицы. Не может быть пустой.
     * @return Объект, представляющий таблицу. Если таблица с указанным именем существует, возвращает null.
     *
     * @throws IllegalArgumentException Если название таблицы null или имеет недопустимое значение. Если список типов
     *                                  колонок null или содержит недопустимые значения.
     * @throws java.io.IOException При ошибках ввода/вывода.
     */
    Table createTable(String name, List<Class<?>> columnTypes) throws IOException;

    /**
     * Удаляет существующую таблицу с указанным названием.
     *
     * Объект удаленной таблицы, если был кем-то взят с помощью {@link #getTable(String)},
     * с этого момента должен бросать {@link IllegalStateException}.
     *
     * @param name Название таблицы.
     *
     * @throws IllegalArgumentException Если название таблицы null или имеет недопустимое значение.
     * @throws IllegalStateException Если таблицы с указанным названием не существует.
     * @throws java.io.IOException - при ошибках ввода/вывода.
     */
    void removeTable(String name) throws IOException;

    /**
     * Преобразовывает строку в объект {@link storeable.Storeable}, соответствующий структуре таблицы.
     *
     * @param table Таблица, которой должен принадлежать {@link storeable.Storeable}.
     * @param value Строка, из которой нужно прочитать {@link storeable.Storeable}.
     * @return Прочитанный {@link storeable.Storeable}.
     *
     * @throws java.text.ParseException - при каких-либо несоответстиях в прочитанных данных.
     */
    Storeable deserialize(Table table, String value) throws ParseException;

    /**
     * Преобразовывает объект {@link storeable.Storeable} в строку.
     *
     * @param table Таблица, которой должен принадлежать {@link storeable.Storeable}.
     * @param value {@link storeable.Storeable}, который нужно записать.
     * @return Строка с записанным значением.
     *
     * @throws storeable.ColumnFormatException При несоответствии типа в {@link storeable.Storeable} и типа колонки в таблице.
     */
    String serialize(Table table, Storeable value) throws ColumnFormatException;

    /**
     * Создает новый пустой {@link storeable.Storeable} для указанной таблицы.
     *
     * @param table Таблица, которой должен принадлежать {@link storeable.Storeable}.
     * @return Пустой {@link storeable.Storeable}, нацеленный на использование с этой таблицей.
     */
    Storeable createFor(Table table);

    /**
     * Создает новый {@link storeable.Storeable} для указанной таблицы, подставляя туда переданные значения.
     *
     * @param table Таблица, которой должен принадлежать {@link storeable.Storeable}.
     * @param values Список значений, которыми нужно проинициализировать поля Storeable.
     * @return {@link storeable.Storeable}, проинициализированный переданными значениями.
     * @throws storeable.ColumnFormatException При несоответствии типа переданного значения и колонки.
     * @throws IndexOutOfBoundsException При несоответствии числа переданных значений и числа колонок.
     */
    Storeable createFor(Table table, List<?> values) throws ColumnFormatException, IndexOutOfBoundsException;

    /**
     * Возвращает имена существующих таблиц, которые могут быть получены с помощью {@link #getTable(String)}.
     *
     * @return Имена существующих таблиц.
     */
    List<String> getTableNames();
}
