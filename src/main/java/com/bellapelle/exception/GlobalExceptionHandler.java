package com.bellapelle.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(
            RecursoNaoEncontradoException e) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("mensagem", e.getMessage()));
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Map<String, String>> regra(
            RegraNegocioException e) {

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("mensagem", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validacao(
            MethodArgumentNotValidException e) {

        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));

        return ResponseEntity.badRequest()
                .body(Map.of("mensagem", msg));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> jsonInvalido(
            HttpMessageNotReadableException e) {

        return ResponseEntity.badRequest()
                .body(Map.of(
                        "mensagem",
                        "Dados inválidos (verifique datas e números)."
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> integridade(
            DataIntegrityViolationException e) {

        String msg =
                "Não foi possível concluir a operação porque os dados violam uma restrição do cadastro. "
                + "Verifique campos obrigatórios, duplicidades e vínculos com outros registros.";

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("mensagem", msg));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> parametroInvalido(
            MethodArgumentTypeMismatchException e) {

        String msg = "Parâmetro '" + e.getName()
                + "' inválido. Verifique o formato informado.";

        return ResponseEntity.badRequest()
                .body(Map.of("mensagem", msg));
    }
}