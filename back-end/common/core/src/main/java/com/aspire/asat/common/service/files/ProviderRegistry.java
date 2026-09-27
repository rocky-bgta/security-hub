package com.aspire.asat.common.service.files;



import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProviderRegistry {
    private final ObjectProvider<StorageProvider> providers;
    private Map<String, StorageProvider> map;

    private Map<String, StorageProvider> map() {
        if (map == null) {
            map = providers.stream().collect(Collectors.toMap(
                    p -> p.providerName().toLowerCase(), Function.identity()
            ));
        }
        return map;
    }

    public StorageProvider get(String name) {
        StorageProvider p = map().get(name.toLowerCase());
        if (p == null) throw new IllegalArgumentException("Unknown storage provider: " + name);
        return p;
    }
}
