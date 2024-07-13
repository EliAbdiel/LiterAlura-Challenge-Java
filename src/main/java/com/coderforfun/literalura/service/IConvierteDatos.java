package com.coderforfun.literalura.service;

public interface IConvierteDatos {

    <T> T obtenerDatos(String json, Class<T> clase);
}
