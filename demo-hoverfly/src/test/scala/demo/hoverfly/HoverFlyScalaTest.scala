package demo.hoverfly

import java.net.{InetSocketAddress, Proxy}
import java.nio.file.Paths

import io.specto.hoverfly.junit.core.{Hoverfly, HoverflyMode, SimulationSource}
import io.specto.hoverfly.junit.core.HoverflyConfig.localConfigs
import okhttp3._
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.junit.JUnitRunner
import software.purpledragon.xml.scalatest.XmlMatchers.beXml

import scala.util.{Failure, Success, Try}

@RunWith(classOf[JUnitRunner])
class HoverFlyScalaTest extends AnyFunSuite {
  private val CERTS_FOLDER_PATH = "hoverfly/certs"
  private val CERTIFICATE_PATH = s"${CERTS_FOLDER_PATH}/my_cert.pem"
  private val KEY_PATH = s"${CERTS_FOLDER_PATH}/my_key.pem"
  private val SIMULATION_PATH = Paths.get("simulation.json").toAbsolutePath

  private val configs =
    localConfigs.logToStdOut
            .overrideDefaultCaCert(CERTIFICATE_PATH, KEY_PATH)
            .proxyPort(8501)
            .adminPort(9999)

  def using[A <: AutoCloseable, B](resource: A)(block: A => B): B =
    Try(block(resource)) match {
      case Success(result) =>
        resource.close()
        result
      case Failure(e) =>
        resource.close()
        throw e
    }

  test("Simulate request") {

    using(new Hoverfly(configs, HoverflyMode.SIMULATE)) { hc: Hoverfly =>
      hc.start()

      hc.simulate(SimulationSource.file(SIMULATION_PATH))

      val client: OkHttpClient =
        new OkHttpClient().newBuilder
          .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8501)))
          .build

      val mediaType = "text/xml"


      val body: RequestBody =
        RequestBody.create(
          MediaType.parse(mediaType),
          """<?xml version="1.0" encoding="utf-8"?>
                     |<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                     |  <soap:Body>
                     |    <NumberToWords xmlns="http://www.dataaccess.com/webservicesserver/">
                     |      <ubiNum>500</ubiNum>
                     |    </NumberToWords>
                     |  </soap:Body>
                     |</soap:Envelope>""".stripMargin
        )

      val request = new Request.Builder()
        .url("https://www.dataaccess.com/webservicesserver/NumberConversion.wso")
        .method("POST", body)
        .addHeader("Content-Type", mediaType)
        .build()

      val response: Response = client.newCall(request).execute()
      val resultXmlElement = scala.xml.XML.loadString(response.body().string())
      val expectedXmlElement =
        <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
        <soap:Body>
          <m:NumberToWordsResponse xmlns:m="http://www.dataaccess.com/webservicesserver/">
            <m:NumberToWordsResult>six hundred </m:NumberToWordsResult>
          </m:NumberToWordsResponse>
        </soap:Body>
      </soap:Envelope>

      resultXmlElement should beXml(expectedXmlElement)


    }

  }



}
