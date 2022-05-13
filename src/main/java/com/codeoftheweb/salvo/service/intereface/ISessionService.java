package com.codeoftheweb.salvo.service.intereface;

import com.codeoftheweb.salvo.dto.response.UserTokenDTO;

import java.security.KeyStoreException;

public interface ISessionService {
    /**
     * Realiza la validación del usuario y contraseña ingresado.
     * En caso de ser correcto, devuelve la cuenta con el token necesario para realizar las demás consultas.
     *
     * @param username User's Email
     * @param password Password to encryted
     * @return UserTokenDTO
     */
    UserTokenDTO login ( String username, String password ) throws KeyStoreException;
}
