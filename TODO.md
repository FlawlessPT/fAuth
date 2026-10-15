# TODO — fAuth

Pendências conhecidas deste plugin (16/10/2026). Riscar quando feito.

Feito na parte 4 (16/10/2026): migração para o MySQL (`AuthRepository` + `AuthService`, BCrypt fora da thread principal), defeitos conhecidos corrigidos (`/register` com `equals`, títulos por jogador, comandos seguros na consola, evento 2FA na thread principal, 2FA já não dispensa a password) e testes automáticos (39).

## Segurança (adiado por decisão de 15/10/2026: projeto de testes, repositórios tratados como privados)
- [ ] O código 2FA do admin está no código-fonte (`AuthorizedUsers.ADMIN_CODES`, por nome): mudar para base de dados (em hash) ou configuração, e identificar o admin por UUID.
- [ ] Limitar as tentativas do código 2FA (4 dígitos = 10 000 combinações) com bloqueio temporário.
- [ ] Bloquear comandos até o jogador se autenticar (só `/login` e `/register`) e rever outros eventos (dano, inventário, largar itens).
- [ ] 2FA a sério (TOTP: código que muda a cada 30 segundos) em vez de um código fixo.
- [ ] Um admin ainda não registado pode ser reclamado por quem registar esse nome primeiro.
- [ ] Um admin que se regista entra sem passar pelo 2FA.

## Fail-closed e `/reload`
- [ ] O `/reload` não é suportado: os títulos ficam no ecrã depois do reload e o estado em memória perde-se.
- [ ] O fail-closed só cobre a BD em baixo. Com o jar do fDatabase ausente o Bukkit não carrega o fAuth (`UnknownDependencyException`), não há autenticação nenhuma e o servidor corre aberto.

## Código
- [ ] Mover `PlayerJoinListener`, `PlayerQuitListener` e `LoggedUsers` para um sítio centralizado (comentários `TODO` no código).
- [ ] Melhorar o registo (log) de utilizadores registados (`RegisterCommand`).
- [ ] Muito estado estático (`LoggedUsersImpl.loggedUsers`, `AuthorizedUsers`): passar a instâncias.

## Qualidade
- [ ] O `auth.db` antigo continua em `plugins/fAuth/` no servidor de testes, sem uso.
