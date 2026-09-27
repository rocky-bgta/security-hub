import com.aspire.asat.registration.constant.WebApiUrlConstants;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WebApiUrlConstantsTest {

    @Test
    void testApiConstants() {

        assertEquals("/api", WebApiUrlConstants.API_PREFIX);
        assertEquals("/v1", WebApiUrlConstants.API_VERSION);
        assertEquals("/api/v1", WebApiUrlConstants.API_URI_ROOT);
        assertEquals("/{id}", WebApiUrlConstants.PATH_VAR_ID);

    }

    @Test
    void testClientApiConstants() {

        assertEquals("/api/v1/client", WebApiUrlConstants.CLIENT_API);

    }

    @Test
    void testMspApiConstants() {

        assertEquals("/api/v1/msp", WebApiUrlConstants.MSP_API);

    }

    @Test
    void testContentApiConstants() {

        assertEquals("/api/v1/content", WebApiUrlConstants.CONTENT_API);

    }

    @Test
    void testLicenseApiConstants() {

        assertEquals("/api/v1/license", WebApiUrlConstants.LICENSE_API);
        assertEquals("/count", WebApiUrlConstants.LICENSE_COUNT);

    }

    @Test
    void testApiPrefixConstant() {

        assertEquals("/api", WebApiUrlConstants.API_PREFIX, "API_PREFIX constant should be /api");

    }

    @Test
    void testApiVersionConstant() {

        assertEquals("/v1", WebApiUrlConstants.API_VERSION, "API_VERSION constant should be /v1");

    }

    @Test
    void testApiUriRootConstant() {

        assertEquals("/api/v1", WebApiUrlConstants.API_URI_ROOT, "API_URI_ROOT constant should be /api/v1");

    }

    @Test
    void testPathVarIdConstant() {

        assertEquals("/{id}", WebApiUrlConstants.PATH_VAR_ID, "PATH_VAR_ID constant should be /{id}");

    }

    @Test
    void testClientApiConstant() {

        assertEquals("/api/v1/client", WebApiUrlConstants.CLIENT_API, "CLIENT_API constant should be /api/v1/client");

    }

    @Test
    void testMspApiConstant() {

        assertEquals("/api/v1/msp", WebApiUrlConstants.MSP_API, "MSP_API constant should be /api/v1/msp");

    }

    @Test
    void testContentApiConstant() {

        assertEquals("/api/v1/content", WebApiUrlConstants.CONTENT_API, "CONTENT_API constant should be /api/v1/content");

    }

    @Test
    void testLicenseApiConstant() {

        assertEquals("/api/v1/license", WebApiUrlConstants.LICENSE_API, "LICENSE_API constant should be /api/v1/license");

    }

    @Test
    void testLicenseCountConstant() {

        assertEquals("/count", WebApiUrlConstants.LICENSE_COUNT, "LICENSE_COUNT constant should be /count");

    }

}