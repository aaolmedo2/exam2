# API REST - Ventanilla Bancaria

## Descripción
API REST desarrollada en Spring Boot + MongoDB para la gestión de efectivo en ventanillas bancarias.

## Características Principales

### Generación Automática de Código de Turno
- **Formato**: `CAJ##-USU##-YYYYMMDD`
- **Ejemplo**: `CAJ01-USU01-20250709`
- **Componentes**:
  - `CAJ##`: Código de caja (CAJ01, CAJ02, etc.)
  - `USU##`: Código de cajero (USU01, USU02, etc.)
  - `YYYYMMDD`: Fecha actual (año-mes-día)

### Denominaciones Válidas
- $1, $5, $10, $20, $50, $100

### Flujo de Operaciones
1. **Iniciar Turno**: El cajero registra el dinero recibido de la bóveda
2. **Procesar Transacciones**: Registro de depósitos y retiros con denominaciones
3. **Cerrar Turno**: Validación de efectivo y detección de diferencias

## Endpoints Disponibles

### 1. Iniciar Turno
```
POST /api/v1/ventanilla-bancaria/iniciar-turno
```

**Request Body:**
```json
{
  "codigoCaja": "CAJ01",
  "codigoCajero": "USU01",
  "denominaciones": [
    {"valor": 100, "cantidad": 5},
    {"valor": 50, "cantidad": 10},
    {"valor": 20, "cantidad": 15},
    {"valor": 10, "cantidad": 20},
    {"valor": 5, "cantidad": 25},
    {"valor": 1, "cantidad": 50}
  ]
}
```

**Response:**
```json
{
  "success": true,
  "message": "Turno iniciado exitosamente",
  "data": {
    "codigoTurno": "CAJ01-USU01-20250709",
    "codigoCaja": "CAJ01",
    "codigoCajero": "USU01",
    "fechaInicio": "2025-07-09T08:00:00Z",
    "fechaFin": null,
    "montoInicial": 1000.00,
    "estado": "ABIERTO"
  }
}
```

### 2. Procesar Transacción
```
POST /api/v1/ventanilla-bancaria/procesar-transaccion
```

**Request Body:**
```json
{
  "codigoTurno": "CAJ01-USU01-20250709",
  "tipoTransaccion": "DEPOSITO",
  "denominaciones": [
    {"valor": 100, "cantidad": 3},
    {"valor": 50, "cantidad": 4}
  ]
}
```

**Response:**
```json
{
  "success": true,
  "message": "Transacción procesada exitosamente",
  "data": {
    "codigoTransaccion": "TXN-001",
    "codigoTurno": "CAJ01-USU01-20250709",
    "tipoTransaccion": "DEPOSITO",
    "montoTotal": 500.00,
    "fechaTransaccion": "2025-07-09T10:30:00Z",
    "denominaciones": [
      {"valor": 100, "cantidad": 3},
      {"valor": 50, "cantidad": 4}
    ]
  }
}
```

### 3. Cerrar Turno
```
POST /api/v1/ventanilla-bancaria/cerrar-turno
```

**Request Body:**
```json
{
  "codigoTurno": "CAJ01-USU01-20250709",
  "denominaciones": [
    {"valor": 100, "cantidad": 8},
    {"valor": 50, "cantidad": 14},
    {"valor": 20, "cantidad": 15},
    {"valor": 10, "cantidad": 20},
    {"valor": 5, "cantidad": 25},
    {"valor": 1, "cantidad": 50}
  ]
}
```

**Response:**
```json
{
  "success": true,
  "message": "Turno cerrado exitosamente",
  "data": {
    "codigoTurno": "CAJ01-USU01-20250709",
    "codigoCajero": "USU01",
    "fechaInicio": "2025-07-09T08:00:00Z",
    "fechaFin": "2025-07-09T16:00:00Z",
    "montoInicial": 1000.00,
    "montoFinal": 1300.00,
    "estado": "CERRADO",
    "alertaCierre": false,
    "diferenciaCierre": 0.00
  }
}
```

## Validaciones y Alertas

### Validaciones de Entrada
- **Código de Caja**: Debe seguir el formato `CAJ##` (ej: CAJ01, CAJ02)
- **Código de Cajero**: Debe seguir el formato `USU##` (ej: USU01, USU02)
- **Denominaciones**: Solo se permiten valores de 1, 5, 10, 20, 50, 100
- **Cantidad**: Debe ser un número entero positivo

### Alerta de Cierre
- La aplicación compara el monto final calculado vs el monto ingresado
- Si hay diferencias, se activa `alertaCierre: true`
- El campo `diferenciaCierre` muestra la diferencia encontrada