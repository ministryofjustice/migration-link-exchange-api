package uk.gov.justice.digital.migrationlinkexchangeapi.integration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.migrationlinkexchangeapi.MigrationLinkExchangeApi

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = [MigrationLinkExchangeApi::class])
@ActiveProfiles("test")
abstract class IntegrationTestBase {

  @Autowired
  protected lateinit var webTestClient: WebTestClient
}
