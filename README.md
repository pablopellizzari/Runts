# Runts

Ecossistema Runts com aplicativo Android exclusivo para atletas e painel web para treinadores.

## Configuração local do Neon

Copie `local.properties.example` para `local.properties` (preservando o `sdk.dir`) e preencha `NEON_HOST`, `NEON_DATABASE`, `NEON_USER` e `NEON_PASSWORD`. Esse arquivo não deve ser versionado. Ao iniciar, o app cria de forma idempotente as tabelas necessárias; a referência completa do esquema está em `database/schema.sql`.

O app usa o endpoint HTTPS serverless do Neon, e não JDBC: o driver PostgreSQL para Java SE depende de APIs ausentes no Android. Para publicar em produção, mova autenticação e acesso ao Neon para uma API/backend ou habilite Data API com RLS: uma credencial de banco em qualquer APK pode ser extraída. A credencial que estava anteriormente no código deve ser revogada/rotacionada no Neon.

## Verificação

`./gradlew :app:testDebugUnitTest :app:assembleDebug` executa os testes e gera o APK de depuração. O teste do Neon só é executado quando as propriedades locais estão preenchidas; sem elas, é ignorado com segurança.

## Painel do treinador

O diretório `coach-web` contém o painel em React e o backend em Node.js/Express. O frontend fica em `coach-web/src/client`, a API em `coach-web/src/server` e os comandos operacionais em `coach-web/scripts`. Copie `coach-web/.env.example` para `coach-web/.env`, configure `JWT_SECRET` e, em produção, `DATABASE_URL`. No desenvolvimento local, o backend também reconhece as propriedades Neon já existentes em `local.properties`.

```text
cd coach-web
npm install
npm run migrate
npm run dev
```

O painel oferece cadastro/login exclusivo para treinador, alunos vinculados, navegação semanal de domingo a sábado no fuso do treinador, criação e edição de treinos, segmentos reordenáveis, favoritos e código de vinculação. O esquema compartilhado fica em `database/schema.sql` e as alterações incrementais em `database/migrations`.

## Organização do projeto

```text
Runts/
├── app/                         # aplicativo Android para atletas
│   └── src/main/java/.../runts/
│       ├── data/                # Room, Neon, segurança e repositórios concretos
│       ├── di/                  # configuração de injeção de dependências
│       ├── domain/              # modelos, contratos e regras de negócio
│       └── ui/                  # telas, componentes, navegação e view models
├── coach-web/
│   ├── src/client/              # interface React do treinador
│   ├── src/server/              # API Node.js/Express
│   └── scripts/                 # scripts operacionais, como migrações
├── database/
│   ├── schema.sql               # referência integral do banco compartilhado
│   └── migrations/              # evoluções incrementais do esquema e dos dados
└── gradle/                      # configuração do projeto Android
```

## Recursos entregues

- cadastro/login de atleta no aplicativo e de treinador no painel web;
- vínculo por código de convite;
- planilhas, aplicação de treinos e calendário de provas;
- registro de treino realizado, PSE e atualização do treino prescrito;
- persistência local Room, fila de sincronização e sincronização com Neon;
- estatísticas calculadas a partir de execuções reais;
- dados de GPS/saúde cifrados com AES-GCM e Android Keystore.

Integrações Strava/Garmin/Apple/Polar/Coros/Samsung não são simuladas: exigem OAuth e webhooks no backend e permanecem explicitamente indisponíveis na interface até essa camada ser implementada.
