package com.proyectparser.core.domain.policy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiagramOptionsTest {

    @Test
    void vistaGeneralEsResumenPorCapasSinDependenciasNiHuerfanos() {
        DiagramOptions opciones = DiagramOptions.overview().build();
        assertTrue(opciones.isSummary());
        assertTrue(opciones.isHideOrphans());
        assertFalse(opciones.isShowDependencies());
        assertFalse(opciones.isShowExternal());
        assertEquals(DiagramOptions.DEFAULT_LAYERS.size(), opciones.getLayers().size());
        assertFalse(opciones.isShortSignatures());
    }

    @Test
    void vistaDeModuloMuestraMiembrosSinAccesoresYConFirmasCortas() {
        DiagramOptions opciones = DiagramOptions.moduleView().build();
        assertFalse(opciones.isSummary());
        assertFalse(opciones.isHideOrphans());
        assertFalse(opciones.isShowDependencies());
        assertFalse(opciones.isShowGettersSetters());
        assertTrue(opciones.isShortSignatures());
        assertFalse(opciones.isShowExternal());
        assertEquals(DiagramOptions.DEFAULT_LAYERS.size(), opciones.getLayers().size());
    }

    @Test
    void completoEsTodoVisibleSalvoExternas() {
        DiagramOptions opciones = DiagramOptions.full().build();
        assertFalse(opciones.isShowExternal());
        assertTrue(opciones.isShowDependencies());
        assertTrue(opciones.isShowGettersSetters());
        assertTrue(opciones.isShowMethods());
        assertFalse(opciones.isSummary());
        assertFalse(opciones.isHideOrphans());
        assertFalse(opciones.isShortSignatures());
        assertTrue(opciones.getLayers().isEmpty());
    }

    @Test
    void losFlagsSeAplicanEncimaDelPreset() {
        DiagramOptions opciones = DiagramOptions.overview().summary(false).build();
        assertFalse(opciones.isSummary());
        assertTrue(opciones.isHideOrphans());
    }
}
