package pt.flawless.fAuth.services;

/**
 * Sessão lembrada de um utilizador: o IP de onde fez login e o instante (epoch ms) em que deixa de valer.
 */
public record RememberedSession(String ip, long untilMillis) {

    /** @return se a sessão vale para este IP no instante dado (o prazo é exclusivo) */
    public boolean isValidFor(String ip, long nowMillis) {
        return this.ip.equals(ip) && nowMillis < untilMillis;
    }
}
