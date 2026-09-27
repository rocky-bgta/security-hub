package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.PayloadTypeChannel;
import com.aspire.asat.phishing.dto.enums.TemplateType;
import com.aspire.asat.phishing.dto.request.PayloadTypeCreateRequest;
import com.aspire.asat.phishing.dto.request.PayloadTypeUpdateRequest;
import com.aspire.asat.phishing.dto.response.PayloadTypeDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.model.PayloadType;
import com.aspire.asat.phishing.repository.PayloadTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayloadTypeServiceImplChannelTest {

    @Mock
    private PayloadTypeRepository payloadTypeRepository;

    @InjectMocks
    private PayloadTypeServiceImpl payloadTypeService;

    @Test
    void createPayloadType_WithoutChannel_DefaultsToEmail() {
        PayloadTypeCreateRequest request = PayloadTypeCreateRequest.builder()
                .name("Phishing Link")
                .displayOrder(1)
                .build();
        when(payloadTypeRepository.existsByNameIgnoreCaseAndEmailChannel(anyString())).thenReturn(false);
        when(payloadTypeRepository.save(any(PayloadType.class))).thenAnswer(invocation -> {
            PayloadType entity = invocation.getArgument(0);
            entity.setId("pt-1");
            return entity;
        });

        PayloadTypeDto created = payloadTypeService.createPayloadType(request);

        assertEquals(PayloadTypeChannel.EMAIL, created.getChannel());
        ArgumentCaptor<PayloadType> captor = ArgumentCaptor.forClass(PayloadType.class);
        verify(payloadTypeRepository).save(captor.capture());
        assertEquals(PayloadTypeChannel.EMAIL, captor.getValue().getChannel());
    }

    @Test
    void createPayloadType_WithSmsChannel_PersistsSmsChannel() {
        PayloadTypeCreateRequest request = PayloadTypeCreateRequest.builder()
                .name("Smishing Link")
                .displayOrder(1)
                .channel(PayloadTypeChannel.SMS)
                .build();
        when(payloadTypeRepository.existsByNameIgnoreCaseAndSmsChannel(anyString())).thenReturn(false);
        when(payloadTypeRepository.save(any(PayloadType.class))).thenAnswer(invocation -> {
            PayloadType entity = invocation.getArgument(0);
            entity.setId("pt-sms-1");
            return entity;
        });

        PayloadTypeDto created = payloadTypeService.createPayloadType(request);

        assertEquals(PayloadTypeChannel.SMS, created.getChannel());
    }

    @Test
    void createPayloadType_DuplicateNameWithinSameChannel_ThrowsDuplicateException() {
        PayloadTypeCreateRequest request = PayloadTypeCreateRequest.builder()
                .name("Duplicate")
                .displayOrder(1)
                .channel(PayloadTypeChannel.SMS)
                .build();
        when(payloadTypeRepository.existsByNameIgnoreCaseAndSmsChannel(anyString())).thenReturn(true);

        assertThrows(DuplicateDataFoundException.class, () -> payloadTypeService.createPayloadType(request));
        verify(payloadTypeRepository, never()).save(any());
    }

    @Test
    void getPayloadTypes_DefaultEmailChannel_UsesEmailRepositoryQuery() {
        PayloadType legacy = PayloadType.builder().id("pt-legacy").name("Legacy").build();
        when(payloadTypeRepository.findWhereEffectiveActiveByEmailChannel(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(legacy)));

        List<PayloadTypeDto> results = payloadTypeService.getPayloadTypes(
                null, true, 0, 10, "displayOrder", "asc", null);

        assertEquals(1, results.size());
        assertEquals(PayloadTypeChannel.EMAIL, results.get(0).getChannel());
        verify(payloadTypeRepository).findWhereEffectiveActiveByEmailChannel(any(Pageable.class));
    }

    @Test
    void getPayloadTypes_SmsChannel_UsesSmsRepositoryQuery() {
        PayloadType sms = PayloadType.builder()
                .id("pt-sms")
                .name("SMS Type")
                .channel(PayloadTypeChannel.SMS)
                .build();
        when(payloadTypeRepository.findWhereEffectiveActiveBySmsChannel(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sms)));

        List<PayloadTypeDto> results = payloadTypeService.getPayloadTypes(
                null, true, 0, 10, "displayOrder", "asc", PayloadTypeChannel.SMS);

        assertEquals(1, results.size());
        assertEquals(PayloadTypeChannel.SMS, results.get(0).getChannel());
        verify(payloadTypeRepository).findWhereEffectiveActiveBySmsChannel(any(Pageable.class));
    }

    @Test
    void getPayloadTypes_VoiceChannel_UsesVoiceRepositoryQuery() {
        PayloadType voice = PayloadType.builder()
                .id("pt-voice")
                .name("Voice Type")
                .channel(PayloadTypeChannel.VOICE)
                .build();
        when(payloadTypeRepository.findWhereEffectiveActiveByVoiceChannel(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(voice)));

        List<PayloadTypeDto> results = payloadTypeService.getPayloadTypes(
                null, true, 0, 10, "displayOrder", "asc", PayloadTypeChannel.VOICE);

        assertEquals(1, results.size());
        assertEquals(PayloadTypeChannel.VOICE, results.get(0).getChannel());
        verify(payloadTypeRepository).findWhereEffectiveActiveByVoiceChannel(any(Pageable.class));
    }

    @Test
    void createPayloadType_WithVoiceChannel_PersistsVoiceChannel() {
        PayloadTypeCreateRequest request = PayloadTypeCreateRequest.builder()
                .name("Vishing Prompt")
                .displayOrder(1)
                .channel(PayloadTypeChannel.VOICE)
                .build();
        when(payloadTypeRepository.existsByNameIgnoreCaseAndVoiceChannel(anyString())).thenReturn(false);
        when(payloadTypeRepository.save(any(PayloadType.class))).thenAnswer(invocation -> {
            PayloadType entity = invocation.getArgument(0);
            entity.setId("pt-voice-1");
            return entity;
        });

        PayloadTypeDto created = payloadTypeService.createPayloadType(request);

        assertEquals(PayloadTypeChannel.VOICE, created.getChannel());
        verify(payloadTypeRepository).existsByNameIgnoreCaseAndVoiceChannel(anyString());
    }

    @Test
    void updatePayloadType_ClearDefault_IsScopedToChannel() {
        PayloadType existing = PayloadType.builder()
                .id("pt-1")
                .name("Existing")
                .displayOrder(1)
                .channel(PayloadTypeChannel.SMS)
                .build();
        PayloadType otherDefault = PayloadType.builder()
                .id("pt-2")
                .name("Other")
                .isDefault(true)
                .channel(PayloadTypeChannel.SMS)
                .build();
        PayloadTypeUpdateRequest request = PayloadTypeUpdateRequest.builder()
                .name("Existing")
                .displayOrder(1)
                .isDefault(true)
                .isActive(true)
                .channel(PayloadTypeChannel.SMS)
                .build();

        when(payloadTypeRepository.findById("pt-1")).thenReturn(Optional.of(existing));
        when(payloadTypeRepository.existsByNameIgnoreCaseAndSmsChannelAndIdNot(anyString(), eq("pt-1")))
                .thenReturn(false);
        when(payloadTypeRepository.findAllByIsDefaultTrueAndSmsChannel()).thenReturn(List.of(otherDefault));
        when(payloadTypeRepository.save(any(PayloadType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        payloadTypeService.updatePayloadType("pt-1", request);

        verify(payloadTypeRepository).findAllByIsDefaultTrueAndSmsChannel();
        verify(payloadTypeRepository, never()).findAllByIsDefaultTrueAndEmailChannel();
    }

    @Test
    void createPayloadType_SameNameDifferentChannels_Allowed() {
        PayloadTypeCreateRequest smsRequest = PayloadTypeCreateRequest.builder()
                .name("Shared Name")
                .displayOrder(1)
                .channel(PayloadTypeChannel.SMS)
                .build();
        when(payloadTypeRepository.existsByNameIgnoreCaseAndSmsChannel(anyString())).thenReturn(false);
        when(payloadTypeRepository.save(any(PayloadType.class))).thenAnswer(invocation -> {
            PayloadType entity = invocation.getArgument(0);
            entity.setId("pt-sms");
            return entity;
        });

        PayloadTypeDto created = payloadTypeService.createPayloadType(smsRequest);

        assertEquals(PayloadTypeChannel.SMS, created.getChannel());
        verify(payloadTypeRepository).existsByNameIgnoreCaseAndSmsChannel(anyString());
        verify(payloadTypeRepository, never()).existsByNameIgnoreCaseAndEmailChannel(anyString());
    }

    @Test
    void updatePayloadType_WithoutChannel_PreservesExistingChannel() {
        PayloadType existing = PayloadType.builder()
                .id("pt-sms")
                .name("Smishing Link")
                .displayOrder(1)
                .channel(PayloadTypeChannel.SMS)
                .isActive(true)
                .isDefault(false)
                .build();
        PayloadTypeUpdateRequest request = PayloadTypeUpdateRequest.builder()
                .name("Smishing Link Updated")
                .displayOrder(2)
                .isDefault(false)
                .isActive(true)
                .channel(null)
                .build();

        when(payloadTypeRepository.findById("pt-sms")).thenReturn(Optional.of(existing));
        when(payloadTypeRepository.existsByNameIgnoreCaseAndSmsChannelAndIdNot(anyString(), eq("pt-sms")))
                .thenReturn(false);
        when(payloadTypeRepository.save(any(PayloadType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PayloadTypeDto updated = payloadTypeService.updatePayloadType("pt-sms", request);

        assertEquals(PayloadTypeChannel.SMS, updated.getChannel());
        assertEquals("Smishing Link Updated", updated.getName());
    }

    @Test
    void getPayloadTypeById_LegacyNullChannel_ReturnsEmailInDto() {
        PayloadType legacy = PayloadType.builder()
                .id("pt-legacy")
                .name("Legacy Type")
                .displayOrder(1)
                .build();
        when(payloadTypeRepository.findById("pt-legacy")).thenReturn(Optional.of(legacy));

        PayloadTypeDto dto = payloadTypeService.getPayloadTypeById("pt-legacy");

        assertEquals(PayloadTypeChannel.EMAIL, dto.getChannel());
    }

    @Test
    void countPayloadTypes_NullChannel_UsesEmailChannelCount() {
        when(payloadTypeRepository.countWhereEffectiveActiveByEmailChannel()).thenReturn(5L);

        long count = payloadTypeService.countPayloadTypes(null, true, null);

        assertEquals(5L, count);
        verify(payloadTypeRepository).countWhereEffectiveActiveByEmailChannel();
        verify(payloadTypeRepository, never()).countWhereEffectiveActiveBySmsChannel();
    }

    @Test
    void createPayloadType_ExistingEmailName_StillUsesEmailUniquenessCheck() {
        PayloadTypeCreateRequest request = PayloadTypeCreateRequest.builder()
                .name("Phishing Link")
                .displayOrder(1)
                .build();
        when(payloadTypeRepository.existsByNameIgnoreCaseAndEmailChannel(anyString())).thenReturn(true);

        assertThrows(DuplicateDataFoundException.class, () -> payloadTypeService.createPayloadType(request));
        verify(payloadTypeRepository).existsByNameIgnoreCaseAndEmailChannel(anyString());
        verify(payloadTypeRepository, never()).existsByNameIgnoreCaseAndSmsChannel(anyString());
    }

    @Test
    void validatePayloadTypeForTemplate_MatchingChannel_DoesNotThrow() {
        when(payloadTypeRepository.findById("pt-email")).thenReturn(Optional.of(
                PayloadType.builder().id("pt-email").name("Link").channel(PayloadTypeChannel.EMAIL).build()));

        payloadTypeService.validatePayloadTypeForTemplate(
                TemplateType.EMAIL, PayloadTypeDto.builder().id("pt-email").build());
    }

    @Test
    void validatePayloadTypeForTemplate_MismatchedChannel_ThrowsValidationException() {
        when(payloadTypeRepository.findById("pt-sms")).thenReturn(Optional.of(
                PayloadType.builder().id("pt-sms").name("Smish").channel(PayloadTypeChannel.SMS).build()));

        assertThrows(PhishingValidationException.class, () -> payloadTypeService.validatePayloadTypeForTemplate(
                TemplateType.EMAIL, PayloadTypeDto.builder().id("pt-sms").build()));
    }

    @Test
    void validatePayloadTypeForTemplate_NullPayloadType_DoesNotThrow() {
        payloadTypeService.validatePayloadTypeForTemplate(TemplateType.EMAIL, null);
    }
}
