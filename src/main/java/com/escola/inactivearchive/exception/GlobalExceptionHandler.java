package com.escola.inactivearchive.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    // Formato de data solicitado: dd/MM/yyyy HH:mm:ss
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final String GENERIC_INTERNAL_ERROR_MESSAGE =
            "Ocorreu um erro interno inesperado. Tente novamente mais tarde.";

    // 2. Método para tratar "Recurso Não Encontrado" (404)
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex,
                                                   HttpServletRequest request) {
        return montarPaginaErro(ex, HttpStatus.NOT_FOUND, request);
    }

    // 3. Método para tratar "Regras de Negócio" (409 - Conflict ou 400 - Bad Request)
    @ExceptionHandler(RegraNegocioException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ModelAndView handleRegraNegocio(RegraNegocioException ex,
                                           HttpServletRequest request) {
        return montarPaginaErro(ex, HttpStatus.CONFLICT, request);
    }

    // 7. Tratamento para URLs estáticas não encontradas (ex: /favicon.ico)
    // Isso evita que erros 404 de imagens/css sujem o log como erro 500
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNoResourceFoundException(NoResourceFoundException ex, HttpServletRequest request) {
        // Podemos retornar a página de erro 404 bonita, mas sem imprimir stack trace no console
        return montarPaginaErro(new RecursoNaoEncontradoException("Página ou recurso não encontrado: " + ex.getResourcePath()), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Erro interno inesperado ao processar a requisição para [{}].",
                request.getRequestURI(), ex);

        return montarPaginaErro(
                new Exception(GENERIC_INTERNAL_ERROR_MESSAGE),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }

    // 5. Método auxiliar para montar o objeto visual (ModelAndView)
    private ModelAndView montarPaginaErro(Exception ex,
                                          HttpStatus status,
                                          HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("error/erro-personalizado");

        mav.addObject("status", status.value());
        mav.addObject("titulo", status.getReasonPhrase()); // Ex: "Not Found", "Internal Server Error"
        mav.addObject("mensagem", ex.getMessage());
        mav.addObject("path", request.getRequestURI());
        mav.addObject("timestamp", LocalDateTime.now().format(FORMATTER));

        return mav;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ModelAndView handleDataIntegrityViolationException(
            HttpServletRequest request
    ) {
        // Personalizamos a mensagem para ficar amigável
        String mensagemAmigavel =
                "Operação não permitida. Provavelmente você está tentando cadastrar um CPF que já existe no sistema.";

        // Reutilizamos nosso método de montar a página, passando a mensagem amigável em vez do erro técnico gigante
        return montarPaginaErro(new Exception(mensagemAmigavel),
                HttpStatus.CONFLICT,
                request);
    }
}
