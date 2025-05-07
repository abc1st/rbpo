package ru.mtuci.babok.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mtuci.babok.model.SignatureEntity;
import ru.mtuci.babok.service.SignatureService;

import java.security.*;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class SignatureServiceImpl  implements SignatureService {
    private final KeyPair keyPair;

    public SignatureServiceImpl() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        this.keyPair = keyGen.generateKeyPair();
    }

    public byte[] sign(byte[] data) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keyPair.getPrivate());
        signature.update(data);
        return signature.sign();
    }

    public boolean verify(byte[] data, byte[] signatureBytes) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(keyPair.getPublic());
        signature.update(data);
        return signature.verify(signatureBytes);
    }

    public byte[] getDataToSign(SignatureEntity entity) {
        String data = entity.getId().toString() +
                entity.getThreatName() +
                new String(entity.getFirstBytes(), StandardCharsets.UTF_8) +
                entity.getRemainderHash() +
                entity.getRemainderLength() +
                entity.getFileType() +
                entity.getOffsetStart() +
                entity.getOffsetEnd() +
                entity.getUpdatedAt().toString() +
                entity.getStatus().name();
        return data.getBytes(StandardCharsets.UTF_8);
    }
}