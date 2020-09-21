package demo.hoverfly;

import com.mashape.unirest.http.exceptions.UnirestException;
import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyConfig;
import io.specto.hoverfly.junit.core.HoverflyMode;
import io.specto.hoverfly.junit.core.SimulationSource;
import okhttp3.*;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.specto.hoverfly.junit.core.HoverflyConfig.localConfigs;

public class HoverflyJavaTest {
    private static final HoverflyMode MODE = HoverflyMode.SIMULATE;
    private static final Path SIMULATION_PATH = Paths.get("simulation.json").toAbsolutePath();



    //sudo $JAVA_HOME/bin/keytool -import -alias hoverfly -keystore $JAVA_HOME/jre/lib/security/cacerts -file cert.pem

    @Test
    public void export() throws IOException {
        try(Hoverfly hoverfly = new Hoverfly(configs(), HoverflyMode.CAPTURE)) {

            hoverfly.start();

            // do some requests here
            OkHttpClient client =
                    new OkHttpClient()
                            .newBuilder()
                            .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1",8501)))
                    .build();
            MediaType mediaType = MediaType.parse("text/xml");
            RequestBody body = RequestBody.create(mediaType, "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n  <soap:Body>\n    <NumberToWords xmlns=\"http://www.dataaccess.com/webservicesserver/\">\n      <ubiNum>500</ubiNum>\n    </NumberToWords>\n  </soap:Body>\n</soap:Envelope>");
            Request request = new Request.Builder()
                    .url("https://www.dataaccess.com/webservicesserver/NumberConversion.wso")
                    .method("POST", body)
                    .addHeader("Content-Type", "text/xml")
                    .build();
            Response response = client.newCall(request).execute();
            assert response.body() != null;
            System.out.println(response.body().string());
            System.out.println("saved to path:"+SIMULATION_PATH.toString());
            hoverfly.exportSimulation(SIMULATION_PATH);
        }
    }

    @Test
    public void shouldBeAbleToGetABookingUsingHoverfly() throws UnirestException, IOException {

        try (Hoverfly hoverfly = new Hoverfly(configs(), MODE)) {

            hoverfly.start();
            hoverfly.simulate(
                    SimulationSource.file(SIMULATION_PATH)
//                    dsl(
//                            service(matches("*"))
//                            .post("webservicesserver/NumberConversion.wso")
//                            .willReturn(success("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
//                                    "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
//                                    "    <soap:Body>\n" +
//                                    "        <m:NumberToWordsResponse xmlns:m=\"http://www.dataaccess.com/webservicesserver/\">\n" +
//                                    "            <m:NumberToWordsResult>six hundred </m:NumberToWordsResult>\n" +
//                                    "        </m:NumberToWordsResponse>\n" +
//                                    "    </soap:Body>\n" +
//                                    "</soap:Envelope>","text/xml"))
//                    )
            );
            // do some requests here

            OkHttpClient client = new OkHttpClient().newBuilder().proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1",8501)))
                    .build();
            MediaType mediaType = MediaType.parse("text/xml");
            RequestBody body = RequestBody.create(mediaType, "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n  <soap:Body>\n    <NumberToWords xmlns=\"http://www.dataaccess.com/webservicesserver/\">\n      <ubiNum>500</ubiNum>\n    </NumberToWords>\n  </soap:Body>\n</soap:Envelope>");
            Request request = new Request.Builder()
                    .url("https://www.dataaccess.com/webservicesserver/NumberConversion.wso")
                    .method("POST", body)
                    .addHeader("Content-Type", "text/xml")
                    .build();
            Response response = client.newCall(request).execute();

            assert response.body().string().contains("six hundred");


        }

    }

    private HoverflyConfig configs() {
        final String certsPath="hoverfly/certs/";
       return  localConfigs().logToStdOut().overrideDefaultCaCert(certsPath+"my_cert.pem",certsPath+"my_key.pem").proxyPort(8501).adminPort(9999);
    }
}
