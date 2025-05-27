package dev.cantrella.ms_wallet.infra.adapter.out.persistence.mapper;

import dev.cantrella.ms_wallet.domain.Wallet;
import dev.cantrella.ms_wallet.infra.adapter.out.persistence.entity.WalletEntity;
import org.springframework.stereotype.Component;

@Component
public class WalletPersistenceMapper {
    public Wallet toDomain(WalletEntity entity) {
        if (entity == null) {
            return null;
        }
        return Wallet.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .balance(entity.getBalance())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public WalletEntity toEntity(Wallet wallet) {
        if (wallet == null) {
            return null;
        }
        return WalletEntity.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .createdAt(wallet.getCreatedAt())
                .build();
    }

}
