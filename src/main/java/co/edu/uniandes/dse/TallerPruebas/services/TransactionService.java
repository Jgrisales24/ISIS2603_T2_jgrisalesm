package co.edu.uniandes.dse.TallerPruebas.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.uniandes.dse.TallerPruebas.entities.AccountEntity;
import co.edu.uniandes.dse.TallerPruebas.entities.TransactionEntity;
import co.edu.uniandes.dse.TallerPruebas.exceptions.BusinessLogicException;
import co.edu.uniandes.dse.TallerPruebas.exceptions.EntityNotFoundException;
import co.edu.uniandes.dse.TallerPruebas.repositories.AccountRepository;
import co.edu.uniandes.dse.TallerPruebas.repositories.TransactionRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    public TransactionEntity createTransaction(Long accountId, TransactionEntity transactionEntity)
            throws EntityNotFoundException, BusinessLogicException {

        log.info("Inicia creación de transacción para la cuenta con id={}", accountId);

        Optional<AccountEntity> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            throw new EntityNotFoundException("La cuenta no existe");
        }

        if (transactionEntity == null) {
            throw new BusinessLogicException("La transacción no puede ser null");
        }

        if (transactionEntity.getMonto() == null || transactionEntity.getMonto() <= 0) {
            throw new BusinessLogicException("El monto debe ser mayor que 0");
        }

        // tipo: ENTRADA o SALIDA 
        if (transactionEntity.getTipo() == null ||
                (!"ENTRADA".equals(transactionEntity.getTipo()) && !"SALIDA".equals(transactionEntity.getTipo()))) {
            throw new BusinessLogicException("El tipo debe ser ENTRADA o SALIDA");
        }

        transactionEntity.setAccount(accountOpt.get());
        TransactionEntity saved = transactionRepository.save(transactionEntity);

        log.info("Termina creación de transacción para la cuenta con id={}", accountId);
        return saved;
    }

    public TransactionEntity getTransaction(Long transactionId) throws EntityNotFoundException {
        Optional<TransactionEntity> txOpt = transactionRepository.findById(transactionId);
        if (txOpt.isEmpty()) {
            throw new EntityNotFoundException("La transacción no existe");
        }
        return txOpt.get();
    }

    public List<TransactionEntity> getTransactions() {
        return transactionRepository.findAll();
    }
}
