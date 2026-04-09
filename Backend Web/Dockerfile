# Etapa 1: Build
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src

# Copiar archivos de proyecto y restaurar dependencias
# Se copia manteniendo la estructura de carpetas para que las referencias relativas funcionen
COPY ["SmartStock.API/SmartStock.API.csproj", "SmartStock.API/"]
COPY ["SmartStock.Application/SmartStock.Application.csproj", "SmartStock.Application/"]
COPY ["SmartStock.Domain/SmartStock.Domain.csproj", "SmartStock.Domain/"]
COPY ["SmartStock.Infrastructure/SmartStock.Infrastructure.csproj", "SmartStock.Infrastructure/"]

RUN dotnet restore "SmartStock.API/SmartStock.API.csproj"

# Copiar todo el código fuente
COPY . .
WORKDIR "/src/SmartStock.API"

# Compilar y publicar
RUN dotnet build "SmartStock.API.csproj" -c Release -o /app/build
RUN dotnet publish "SmartStock.API.csproj" -c Release -o /app/publish /p:UseAppHost=false

# Etapa 2: Runtime
FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS final
WORKDIR /app
EXPOSE 8080
EXPOSE 8081

COPY --from=build /app/publish .
ENTRYPOINT ["dotnet", "SmartStock.API.dll"]
