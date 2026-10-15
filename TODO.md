# TODO — fAuth

Pendências conhecidas deste plugin (15/10/2026). Riscar quando feito.

## Migração para o MySQL (parte 4 da camada de BD): por fazer
- [ ] Trocar `AuthDatabase`/`AuthDatabaseImpl` (SQLite, `auth.db`, estático) por um repositório sobre o `DatabaseService` (`Databases.get()` + `migrate` no `onEnable`; desativar o plugin se a BD falhar). Esquema novo, a partir do zero.
- [ ] Fazer o BCrypt e as consultas fora da thread principal.
- [ ] Corrigir os defeitos conhecidos:
  - `/register` compara as passwords com `equalsIgnoreCase` (deve ser `equals`);
  - `PlayerJoinListener.authTitleAlert` é estático e partilhado entre todos os jogadores;
  - `/login` e `/register` fazem cast direto para `Player` (rebenta na consola) e devolvem sempre `false` (o Bukkit mostra o uso por cima da mensagem própria);
  - o evento de LOGIN do 2FA é disparado na thread assíncrona do chat (quem ouvir o evento e usar a API Bukkit tem de passar para a thread principal);
  - o 2FA dispensa a password: um admin que ainda não fez login e escreve o código no chat fica autenticado (`PlayerChatListener`).

## Segurança (adiado por decisão de 15/10/2026: projeto de testes, repositórios tratados como privados)
- [ ] O código 2FA do admin está no código-fonte (`AuthorizedUsers.ADMIN_CODES`, por nome): mudar para base de dados (em hash) ou configuração, e identificar o admin por UUID.
- [ ] Limitar as tentativas do código 2FA (4 dígitos = 10 000 combinações) com bloqueio temporário.
- [ ] Bloquear comandos até o jogador se autenticar (só `/login` e `/register`) e rever outros eventos (dano, inventário, largar itens).
- [ ] 2FA a sério (TOTP: código que muda a cada 30 segundos) em vez de um código fixo.

## Código
- [ ] Mover `PlayerJoinListener`, `PlayerQuitListener` e `LoggedUsers` para um sítio centralizado (comentários `TODO` no código).
- [ ] Melhorar o registo (log) de utilizadores registados (`RegisterCommand`).
- [ ] Muito estado estático (`LoggedUsersImpl.loggedUsers`, `AuthorizedUsers`): passar a instâncias.

## Qualidade
- [ ] O fAuth não tem testes automáticos.
- [ ] O `auth.db` antigo continua em `plugins/fAuth/` no servidor de testes, sem uso (depois da migração).
