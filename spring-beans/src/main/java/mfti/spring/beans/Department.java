package mfti.spring.beans;

/**
 * Контракт отдела. Контроллер зависит от этого интерфейса.
 * Слово «книга» или «бульдозер» живёт в конкретной реализации, не в контроллере.
 */
public interface Department {

    boolean matches(String body);

    String place(String body);
}
