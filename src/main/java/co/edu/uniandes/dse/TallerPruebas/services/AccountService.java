package co.edu.uniandes.dse.TallerPruebas.services;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniandes.dse.TallerPruebas.entities.AccountEntity;
import co.edu.uniandes.dse.TallerPruebas.entities.TransactionEntity;
import co.edu.uniandes.dse.TallerPruebas.exceptions.BusinessLogicException;
import co.edu.uniandes.dse.TallerPruebas.exceptions.EntityNotFoundException;
import co.edu.uniandes.dse.TallerPruebas.repositories.AccountRepository;
import co.edu.uniandes.dse.TallerPruebas.repositories.TransactionRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * REGLA 2
     * Transferir dinero entre cuentas
     */
    @Transactional
    public void transferMoney(Long originAccountId, Long destinationAccountId, Double monto)
            throws EntityNotFoundException, BusinessLogicException {

        log.info("Inicia transferencia entre cuentas");

        // Validar monto
        if (monto == null || monto <= 0) {
            throw new BusinessLogicException("El monto debe ser mayor que 0");
        }

        // Validar existencia de cuentas
        Optional<AccountEntity> originOpt = accountRepository.findById(originAccountId);
        if (originOpt.isEmpty()) {
            throw new EntityNotFoundException("La cuenta origen no existe");
        }

        Optional<AccountEntity> destOpt = accountRepository.findById(destinationAccountId);
        if (destOpt.isEmpty()) {
            throw new EntityNotFoundException("La cuenta destino no existe");
        }

        AccountEntity origin = originOpt.get();
        AccountEntity destination = destOpt.get();

        // Validar que no sean la misma cuenta
        if (origin.getId().equals(destination.getId())) {
            throw new BusinessLogicException("La cuenta origen y destino no pueden ser la misma");
        }

        // Validar fondos suficientes
        if (origin.getSaldo() == null || origin.getSaldo() < monto) {
            throw new BusinessLogicException("Fondos insuficientes en la cuenta origen");
        }

        // Actualizar saldos
        origin.setSaldo(origin.getSaldo() - monto);

        if (destination.getSaldo() == null) {
            destination.setSaldo(0.0);
        }

        destination.setSaldo(destination.getSaldo() + monto);

        accountRepository.save(origin);
        accountRepository.save(destination);

        // Registrar transacciones
        Date ahora = new Date();

        TransactionEntity salida = new TransactionEntity();
        salida.setMonto(monto);
        salida.setFecha(ahora);
        salida.setTipo("SALIDA");
        salida.setAccount(origin);

        TransactionEntity entrada = new TransactionEntity();
        entrada.setMonto(monto);
        entrada.setFecha(ahora);
        entrada.setTipo("ENTRADA");
        entrada.setAccount(destination);

        transactionRepository.save(salida);
        transactionRepository.save(entrada);

        log.info("Finaliza transferencia entre cuentas");
    }
}
