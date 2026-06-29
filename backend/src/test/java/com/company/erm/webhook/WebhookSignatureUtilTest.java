package com.company.erm.webhook;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class WebhookSignatureUtilTest {

    @Test
    void sign_isDeterministicForSameInput() {
        String signature = WebhookSignatureUtil.sign("secret", "1710000000", "{\"type\":\"leave.approved\"}");
        assertThat(signature).isEqualTo(WebhookSignatureUtil.sign("secret", "1710000000", "{\"type\":\"leave.approved\"}"));
        assertThat(signature).hasSize(64);
    }

    @Test
    void sign_changesWhenPayloadChanges() {
        String first = WebhookSignatureUtil.sign("secret", "1710000000", "{\"a\":1}");
        String second = WebhookSignatureUtil.sign("secret", "1710000000", "{\"a\":2}");
        assertThat(first).isNotEqualTo(second);
    }
}
