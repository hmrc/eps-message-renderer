/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.epsmessagerenderer.connectors

import org.mockito.ArgumentMatchers.any
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.epsmessagerenderer.BaseSpec
import org.mockito.Mockito.{ reset, times, verify, when }
import play.api.Application
import play.api.libs.ws.BodyWritable
import play.api.test.Helpers.*
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.epsmessagerenderer.models.{ MobileNotification, MobileNotificationAudit }
import uk.gov.hmrc.epsmessagerenderer.utils.Auditing
import uk.gov.hmrc.http.client.{ HttpClientV2, RequestBuilder }
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.audit.model.EventTypes

import scala.concurrent.Future

class MobileConnectorSpec extends BaseSpec with WireMockHelper {

  lazy val mockHttp: HttpClientV2 = mock[HttpClientV2]
  val requestBuilder: RequestBuilder = mock[RequestBuilder]
  lazy val mockAuditing: Auditing = mock[Auditing]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockHttp, requestBuilder, mockAuditing)
  }

  override lazy val app: Application = GuiceApplicationBuilder()
    .configure(Map("auditing.enabled" -> false))
    .build()

  lazy val connector: MobileConnector =
    new MobileConnector(mockHttp, servicesConfig, mockAuditing)

  val identifierValue: Nino = nino

  "Mobile Connector" must {

    "checkAndSendNotification when identifier is not registered" in {

      when(mockHttp.get(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any)).thenReturn(
        Future.successful(
          HttpResponse(
            OK,
            s"""{
               |"isRegistered" : false
               |}""".stripMargin,
            Map.empty[String, Seq[String]]
          )
        )
      )

      await(connector.checkAndSendNotification(identifierValue))

      verify(mockAuditing, times(1)).createAudit(
        EventTypes.Failed,
        s"Mobile Notification",
        MobileNotificationAudit(nino, OK, Some(s"$nino is not registered"))
      )

      verify(mockHttp, times(0)).post(any)(any)
      verify(requestBuilder, times(0)).withBody(any)(any[BodyWritable[MobileNotification]], any, any)
    }

    "checkAndSendNotification when identifier is registered" in {

      when(mockHttp.get(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any))
        .thenReturn(
          Future.successful(
            HttpResponse(
              OK,
              s"""{
                 |"isRegistered" : true
                 |}""".stripMargin,
              Map.empty[String, Seq[String]]
            )
          )
        )

      when(mockHttp.post(any)(any)).thenReturn(requestBuilder)

      await(connector.checkAndSendNotification(identifierValue))

      verify(mockHttp, times(1)).post(any)(any)
      verify(requestBuilder, times(1)).withBody(any)(any[BodyWritable[MobileNotification]], any, any)
      verify(requestBuilder, times(1)).execute(any, any)
    }

    "handle non OK status when checking registration" in {

      when(mockHttp.get(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any)).thenReturn(
        Future.successful(
          HttpResponse(INTERNAL_SERVER_ERROR, "Internal Server Error", Map.empty[String, Seq[String]])
        )
      )

      await(connector.checkAndSendNotification(identifierValue))

      verify(mockAuditing, times(1)).createAudit(any, any, any)(any)
    }

    "handle exception when checking registration" in {

      when(mockHttp.get(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any)).thenReturn(
        Future.failed(new RuntimeException("Connection timeout"))
      )

      await(connector.checkAndSendNotification(identifierValue))

      verify(mockAuditing, times(1)).createAudit[Throwable](any, any, any)(any)
    }

    "successfully send notification and audit success" in {

      when(mockHttp.post(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.withBody(any)(any, any, any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any)).thenReturn(
        Future.successful(HttpResponse(OK, "", Map.empty))
      )

      val notification = MobileNotification(identifierValue)
      await(connector.sendNotification(notification))

      verify(mockAuditing, times(1)).createAudit[MobileNotificationAudit](any, any, any)(any)
    }

    "handle exception when sending notification" in {

      when(mockHttp.post(any)(any)).thenReturn(requestBuilder)
      when(requestBuilder.withBody(any)(any, any, any)).thenReturn(requestBuilder)
      when(requestBuilder.execute(any, any)).thenReturn(
        Future.failed(new RuntimeException("Network error"))
      )

      val notification = MobileNotification(identifierValue)
      await(connector.sendNotification(notification))

      verify(mockAuditing, times(1)).createAudit[Throwable](any, any, any)(any)
    }
  }
}
