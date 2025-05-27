package dev.cantrella.ms_wallet.infra.adapter.out.persistence.repository;

import dev.cantrella.ms_wallet.domain.Wallet;
import dev.cantrella.ms_wallet.infra.adapter.out.persistence.entity.WalletEntity;
import dev.cantrella.ms_wallet.infra.adapter.out.persistence.mapper.WalletPersistenceMapper;
import dev.cantrella.ms_wallet.ports.out.WalletRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class WalletRepositoryAdapter implements WalletRepositoryPort {

    private final JpaWalletRepository jpaWalletRepository;
    private final WalletPersistenceMapper mapper;
    @Override
    public boolean existsByUserId(String userId) {
        return jpaWalletRepository.existsByUserId(userId);
    }

    @Override
    public Wallet save(Wallet wallet) {
        WalletEntity entity = mapper.toEntity(wallet);
        WalletEntity savedEntity = jpaWalletRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Wallet> findById(UUID id) {
        return jpaWalletRepository.findById(id)
                .map(mapper::toDomain);
    }
}
