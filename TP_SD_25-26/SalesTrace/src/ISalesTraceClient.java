import exceptions.MensagemErroException;
import exceptions.TentativasExcedidasException;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface ISalesTraceClient {

    boolean signIn(String username, String password, boolean admin) throws TentativasExcedidasException;
    boolean logIn(String username, String password) throws TentativasExcedidasException;
    void addEvent(String product, int stock, float price) throws MensagemErroException;
    int getStockProduct(String product, int days) throws MensagemErroException;
    float getProfitProduct(String product, int days) throws MensagemErroException;
    float getAveragePriceProduct(String product, int days) throws MensagemErroException;
    float getMaximumPriceProduct(String product, int days) throws MensagemErroException;
    List<Event> filterEvents(Set<String> products, int day) throws MensagemErroException;
    boolean notifySimultaneousSales(String p1, String p2) throws MensagemErroException;
    String notifyConsecutiveSales(int n) throws MensagemErroException;
    boolean nextDay() throws MensagemErroException;
    void exit() throws IOException;

}
