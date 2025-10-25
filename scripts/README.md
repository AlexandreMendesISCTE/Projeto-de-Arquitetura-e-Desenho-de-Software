# Scripts de Automação

Esta pasta contém scripts úteis para desenvolvimento, build e deployment.

## 📜 Scripts Disponíveis

### 🔨 Build Scripts

Compilam o projeto usando Maven.

#### Linux/Mac
```bash
./scripts/build.sh
```

#### Windows PowerShell
```powershell
.\scripts\build.ps1
```

---

### 🐳 Docker Scripts

Gerenciam a execução do projeto via Docker.

#### Docker Start (Build + Run)

**Linux/Mac**
```bash
./scripts/docker-start.sh
```

**Windows PowerShell**
```powershell
.\scripts\docker-start.ps1
```

#### Docker Run (Run apenas)

**Linux/Mac**
```bash
./scripts/docker-run.sh
```

**Windows**
```batch
.\scripts\docker-run.bat
```

---

## ⚙️ Configuração

### Permissões (Linux/Mac)

Para executar os scripts bash, dê permissão de execução:

```bash
chmod +x scripts/*.sh
```

---

## 🔗 Links Úteis

- [← Voltar ao README principal](../README.md)
- [📚 Ver Documentação](../docs/)

