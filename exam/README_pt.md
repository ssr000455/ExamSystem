# ExamSystem Manual do Utilizador

## Requisitos do sistema
- Java 8 ou superior
- Ambiente gráfico compatível com OpenGL (Linux: instalar mesa-utils)

## Como iniciar
./start-exam.sh
ou
java -jar ExamSystem.jar

## Funcionalidades
- Selecionar plano de aprendizagem (8 módulos sistemáticos)
- Tipos de perguntas: programação, escolha, preenchimento, correspondência, depuração, completar
- Guardar progresso automaticamente
- Sistema de conquistas e estrelas
- Interface multilíngue (7 idiomas)
- Editor de código integrado (realce de sintaxe, autocompletar)

## Primeira utilização
1. Ecrã de boas-vindas aparece
2. Clique em "Importar plano" para carregar os planos oficiais (ou pule se já existirem)
3. Escolha a dificuldade (Iniciante/Intermediário/Avançado)
4. Comece a responder

## Atalhos
- Ctrl+S: Guardar progresso
- Ctrl+R: Executar verificação de código
- Ctrl+←/→: Pergunta anterior/seguinte
- Esc: Voltar à seleção da secção

## Ficheiros de configuração
O progresso é guardado em .exam_progress.json, .exam_unlocks.json, .exam_achievements.json.

## Resolução de problemas
- Se faltar uma biblioteca nativa, certifique-se de que os controladores OpenGL estão instalados.
- Se não iniciar, verifique a versão do Java: java -version
