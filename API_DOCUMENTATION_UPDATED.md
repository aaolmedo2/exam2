# API REST - Ventanilla Bancaria (Actualizada)

## Endpoints Disponibles

### 1. Iniciar Turno
**POST** `/api/ventanilla/iniciar-turno`

Inicia un nuevo turno para un cajero en una caja específica.

**Request Body:**
```json
{
  "codigoCaja": "CAJ01",
  "codigoCajero": "USU01",
  "denominacionesIniciales": [
    {
      "billete": 100,
      "cantidad": 50
    },
    {
      "billete": 50,
      "cantidad": 30
    },
    {
      "billete": 20,
      "cantidad": 40
    }
  ]
}
```

**Response:**
```json
{
  "success": true,
  "message": "Turno iniciado exitosamente",
  "data": {
    "id": "676dc123a5f123456789abc1",
    "codigoCaja": "CAJ01",
    "codigoCajero": "USU01",
    "codigoTurno": "CAJ01-USU01-20250709",
    "inicioTurno": "2025-07-09T21:00:00.000Z",
    "montoInicial": 8500.0,
    "estado": "ABIERTO",
    "denominacionesIniciales": [
      {
        "billete": 100,
        "cantidad": 50
      },
      {
        "billete": 50,
        "cantidad": 30
      },
      {
        "billete": 20,
        "cantidad": 40
      }
    ],
    "alertaCierre": false
  }
}
```

### 2. Procesar Transacción
**POST** `/api/ventanilla/procesar-transaccion`

Procesa una transacción (depósito o retiro) en el turno activo del cajero.

**IMPORTANTE:** El usuario NO debe enviar el código de turno. La aplicación determina automáticamente el turno activo basándose en la caja y el cajero.

**Request Body:**
```json
{
  "codigoCaja": "CAJ01",
  "codigoCajero": "USU01",
  "tipoTransaccion": "DEPOSITO",
  "denominaciones": [
    {
      "billete": 100,
      "cantidad": 5
    },
    {
      "billete": 50,
      "cantidad": 10
    }
  ]
}
```

**Response:**
```json
{
  "success": true,
  "message": "Transacción procesada exitosamente",
  "data": {
    "id": "676dc123a5f123456789abc2",
    "codigoCaja": "CAJ01",
    "codigoCajero": "USU01",
    "codigoTurno": "CAJ01-USU01-20250709",
    "tipoTransaccion": "DEPOSITO",
    "montoTotal": 1000.0,
    "fechaTransaccion": "2025-07-09T21:30:00.000Z",
    "denominaciones": [
      {
        "billete": 100,
        "cantidad": 5
      },
      {
        "billete": 50,
        "cantidad": 10
      }
    ]
  }
}
```

### 3. Cerrar Turno
**POST** `/api/ventanilla/cerrar-turno`

Cierra un turno específico, registrando el dinero final y comparándolo con el monto teórico.

**Request Body:**
```json
{
  "codigoTurno": "CAJ01-USU01-20250709",
  "denominacionesFinales": [
    {
      "billete": 100,
      "cantidad": 60
    },
    {
      "billete": 50,
      "cantidad": 35
    },
    {
      "billete": 20,
      "cantidad": 45
    }
  ]
}
```

**Response:**
```json
{
  "success": true,
  "message": "Turno cerrado exitosamente",
  "data": {
    "id": "676dc123a5f123456789abc1",
    "codigoCaja": "CAJ01",
    "codigoCajero": "USU01",
    "codigoTurno": "CAJ01-USU01-20250709",
    "inicioTurno": "2025-07-09T21:00:00.000Z",
    "finTurno": "2025-07-09T23:00:00.000Z",
    "montoInicial": 8500.0,
    "montoFinal": 9750.0,
    "montoTeorico": 9750.0,
    "diferenciaCierre": 0.0,
    "estado": "CERRADO",
    "alertaCierre": false
  }
}
```

## Flujo de Trabajo

### 1. Inicio de Turno
- El cajero envía su código y el código de la caja
- Especifica las denominaciones iniciales recibidas de la bóveda
- El sistema genera automáticamente el código de turno con formato: `CAJ##-USU##-YYYYMMDD`
- Se registra la transacción de tipo "INICIO"

### 2. Procesamiento de Transacciones
- **IMPORTANTE:** El usuario NO envía el código de turno
- El sistema busca automáticamente el turno activo para la caja y cajero especificados
- Se valida que exista un turno activo
- Se registra la transacción con las denominaciones recibidas/entregadas
- Los retiros se registran como montos negativos, los depósitos como positivos

### 3. Cierre de Turno
- El cajero cuenta físicamente el dinero y especifica las denominaciones finales
- El sistema calcula el monto teórico basado en las transacciones
- Se compara el monto final declarado con el monto teórico
- Si hay diferencia, se genera una alerta automáticamente

## Validaciones

- **Códigos de Caja:** Deben seguir el formato `CAJ##` (ej: CAJ01, CAJ02)
- **Códigos de Cajero:** Deben seguir el formato `USU##` (ej: USU01, USU02)
- **Códigos de Turno:** Se generan automáticamente con formato `CAJ##-USU##-YYYYMMDD`
- **Denominaciones:** Solo se permiten billetes de 1, 5, 10, 20, 50, 100
- **Tipos de Transacción:** INICIO, DEPOSITO, RETIRO, CIERRE
- **Turnos Activos:** Solo se permite un turno activo por cajero

## Mensajes de Error

- **400 Bad Request:** Datos inválidos (códigos mal formateados, denominaciones inválidas)
- **404 Not Found:** Turno no encontrado o no activo
- **409 Conflict:** El cajero ya tiene un turno abierto
- **500 Internal Server Error:** Error interno del servidor

## Cambios Implementados

1. **Eliminación del código de turno en transacciones:** El usuario ya no necesita enviar el código de turno al procesar transacciones
2. **Búsqueda automática de turno activo:** El sistema busca automáticamente el turno activo basándose en la caja y el cajero
3. **Validación mejorada:** Se valida que exista un turno activo antes de procesar transacciones
4. **Flujo simplificado:** El usuario solo necesita conocer su código de cajero y el código de la caja

Este cambio mejora significativamente la usabilidad de la API y reduce la posibilidad de errores por parte del usuario.
