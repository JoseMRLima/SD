
/**
 * Representa o tipo de mensagens que o servidor reconhece
 */
public enum RequestType {
    ERROR(-1, "ERROR"),
    NONE(0, "ERROR"),
    SIGN_IN(1, "SIGN IN"),
    LOG_IN(2, "LOG IN"),
    ADD_EVENT(3, "ADD EVENT"),
    STOCK(4, "STOCK"),
    TOTAL(5, "TOTAL"),
    AVERAGE(6, "AVERAGE"),
    MAXIMUM(7, "MAXIMUM"),
    FILTER(8, "FILTER"),
    SIMULTANEOUS(9, "SIMULTANEOUS"),
    CONSECUTIVE(10, "CONSECUTIVE"),
    NEXT_DAY(11, "NEXT DAY");

    // variáveis de instância

    private final int value;
    private final String name;


    // construtores

    RequestType(int value, String name) {
        this.value = value;
        this.name = name;
    }


    // métodos de instância

    public int getValue() {
        return this.value;
    }

    public static RequestType fromValue(int value) {
        for (RequestType type : RequestType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return NONE;
    }

    public String toString() {
        return name;
    }

}
