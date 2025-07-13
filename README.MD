# Tausch

*Intercambia items entre jugadores de forma rápida y segura.*

---

## Descripción

Ofrece un sistema robusto, intuitivo y seguro para que los jugadores puedan intercambiar items con confirmación, evitando exploits y con registros almacenados en MongoDB para auditoría.

---

## Características principales

- Tradeo entre jugadores vía `/trade <jugador>`
- Confirmación en dos fases para evitar errores y dupes
- Sistema de ignore para no recibir solicitudes de trade
- Logs completos almacenados en MongoDB para auditoría
- Menús intuitivos y mensajes configurables en múltiples idiomas
- Comandos administrativos para recargar configs y consultar historial
- Optimizaciones para alto rendimiento en servidores grandes
- Sistema flexible y escalable pensado para futuros addons

---

## Comandos

| Comando                   | Uso                             | Permisos       | Descripción                                      |
|---------------------------|--------------------------------|----------------|-------------------------------------------------|
| `/trade <jugador>`        | Enviar solicitud de trade      | `-`            | Envía una solicitud de trade al jugador indicado. |
| `/trade accept`            | Aceptar solicitud pendiente    | `-`    | Acepta la solicitud de trade recibida.          |
| `/trade decline`           | Rechazar solicitud pendiente   | `-`    | Rechaza la solicitud de trade recibida.         |
| `/trade ignore`            | Activar/desactivar ignore trade | `-`     | Alterna si recibes solicitudes de trade.        |
| `/tausch`                  | Mostrar información del plugin | `-`     | Muestra versión, autor y datos del plugin.       |
| `/tausch reload`           | Recargar configuración y mensajes | `tausch.admin` | Recarga los archivos de configuración y mensajes. |
| `/tradehistory <jugador>` | Mostrar historial de tradeos   | `tausch.admin` | Abre menú con el historial de tradeos del jugador. |

---

