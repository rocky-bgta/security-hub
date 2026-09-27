package com.aspire.asat.cms.service.certificate;

import com.aspire.asat.cms.dto.client.responseDto.CertificateDetailsResponseDTO;
import com.aspire.asat.cms.dto.enums.CertificateStatus;
import com.aspire.asat.cms.model.UserCertificate;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CertificateServiceImplCertificateDetailsMappingTest {

    @Test
    void mapToCertificateDetailsResponseDTO_nullExpiry_preservesStoredStatus() throws Exception {
        UserCertificate certificate = UserCertificate.builder()
                .certificateId("CERT-1")
                .status(CertificateStatus.VALID.name())
                .expiryDate(null)
                .build();

        CertificateDetailsResponseDTO dto = invokeMapper(certificate);

        assertEquals(CertificateStatus.VALID.name(), dto.getStatus());
    }

    @Test
    void mapToCertificateDetailsResponseDTO_expiredExpiry_returnsExpiredStatus() throws Exception {
        UserCertificate certificate = UserCertificate.builder()
                .certificateId("CERT-2")
                .status(CertificateStatus.VALID.name())
                .expiryDate(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();

        CertificateDetailsResponseDTO dto = invokeMapper(certificate);

        assertEquals(CertificateStatus.EXPIRED.name(), dto.getStatus());
    }

    @Test
    void mapToCertificateDetailsResponseDTO_nullExpiryAndNullStoredStatus_returnsNullStatus() throws Exception {
        UserCertificate certificate = UserCertificate.builder()
                .certificateId("CERT-3")
                .status(null)
                .expiryDate(null)
                .build();

        CertificateDetailsResponseDTO dto = invokeMapper(certificate);

        assertNull(dto.getStatus());
    }

    private static CertificateDetailsResponseDTO invokeMapper(UserCertificate certificate) throws Exception {
        CertificateServiceImpl service = new CertificateServiceImpl(
                null, null, null, null, null, null, null, null, null, null, null, null, null);
        Method method = CertificateServiceImpl.class.getDeclaredMethod(
                "mapToCertificateDetailsResponseDTO", UserCertificate.class);
        method.setAccessible(true);
        return (CertificateDetailsResponseDTO) method.invoke(service, certificate);
    }
}
