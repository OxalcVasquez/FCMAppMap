package com.vasquez.fernandez.AppTemplateFCM.modelo;

public class Mensaje {
    private String id;
    private String titulo;
    private String textoMensaje;
    private String fechaHora;

    public Mensaje(String id, String titulo, String textoMensaje, String fechaHora) {
        this.id = id;
        this.titulo = titulo;
        this.textoMensaje = textoMensaje;
        this.fechaHora = fechaHora;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTextoMensaje() {
        return textoMensaje;
    }

    public void setTextoMensaje(String textoMensaje) {
        this.textoMensaje = textoMensaje;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }
}
