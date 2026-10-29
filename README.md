# Alarma IoT

**Aplicación Android para la simulación de un sistema de seguridad y monitoreo de compartimentos mediante tecnología IoT.**

Proyecto desarrollado para la asignatura **Aplicaciones Móviles para IoT (TI3042)**.

## 1. Descripción del proyecto

Alarma IoT es una aplicación Android desarrollada en Kotlin que simula un sistema de seguridad destinado a proteger cajas, cajones u otros compartimentos.
El sistema utiliza un sensor de luminosidad LDR simulado para detectar la entrada de luz que se produciría al abrir un compartimento.
Cuando el sistema está armado y se detecta luz, se activa una alarma sonora y un indicador LED rojo simulado.
La aplicación permite monitorear y controlar el sistema desde dos dispositivos Android conectados a Firebase.

## 2. Funcionalidades

- Registro e inicio de sesión mediante Firebase Authentication.
- Monitoreo del estado del sensor LDR simulado.
- Activación automática de la alarma ante una intrusión.
- Reproducción de una sirena de alerta.
- Visualización del estado de un LED rojo simulado.
- Armado y desarmado del sistema de seguridad.
- Silenciamiento de la sirena sin desactivar el indicador de intrusión.
- Sincronización de estados entre dispositivos Android.
- Almacenamiento y actualización de datos mediante Firebase Realtime Database.
- Cierre de sesión del usuario.

## 3. Tecnologías utilizadas

- **Android Studio:** entorno de desarrollo de la aplicación.
- **Kotlin:** lenguaje de programación.
- **XML:** diseño de las interfaces.
- **Firebase Authentication:** registro y autenticación de usuarios.
- **Firebase Realtime Database:** almacenamiento y sincronización de datos.
- **Wi-Fi / Internet:** conectividad utilizada para comunicarse con Firebase.

## 4. Arquitectura de comunicación

La aplicación utiliza Firebase Realtime Database como intermediario para sincronizar los estados del sistema entre dos dispositivos Android.

**Flujo de comunicación:**

Emulador Android ↔ Firebase Realtime Database ↔ Teléfono Android

En las pruebas se utilizaron:

- **Emulador Android Studio:** simula la detección de luz del sensor LDR.
- **Samsung SM-A055M:** recibe las alertas, reproduce la sirena y permite controlar el sistema.

Ambos dispositivos ejecutan la misma aplicación, por lo que pueden consultar y modificar el estado de la alarma.

La comunicación es bidireccional y requiere conexión a Internet. No se utiliza una conexión directa entre dispositivos mediante Bluetooth o Wi-Fi Direct.

## 5. Funcionamiento de la alarma

1. El sistema comienza armado y protegido.
2. El usuario puede simular la entrada de luz mediante un interruptor.
3. Si el sistema está armado y detecta luz, se activa la alarma.
4. La aplicación reproduce una sirena y muestra el LED rojo encendido.
5. El usuario puede silenciar la sirena sin desarmar el sistema.
6. También puede desarmar el sistema para autorizar la apertura del compartimento.
7. Para volver a armarlo, el compartimento debe estar cerrado, es decir, sin luz detectada.

Los cambios se sincronizan entre los dispositivos que utilizan la misma cuenta.

## 6. Seguridad

El proyecto incorpora medidas de seguridad relacionadas con las recomendaciones de ISO/IEC 27400 para sistemas IoT:

- Autenticación de usuarios mediante Firebase Authentication.
- Acceso a las funciones de monitoreo condicionado a una sesión iniciada.
- Reglas de Firebase Realtime Database que restringen la lectura y escritura de datos al usuario autenticado correspondiente.
- Separación de la información mediante el identificador único de cada usuario (UID).
- Comunicación con los servicios de Firebase mediante conexiones cifradas.
- Gestión de credenciales mediante Firebase Authentication, sin almacenar contraseñas directamente en el código de la aplicación.

Estas medidas corresponden al alcance de un prototipo académico y no constituyen una certificación de cumplimiento de ISO/IEC 27400.

## 7. Pruebas realizadas

Se realizaron pruebas funcionales utilizando un emulador Android y un teléfono Samsung físico.

Se comprobó:

- Inicio de sesión mediante Firebase Authentication.
- Lectura y actualización de datos en Firebase Realtime Database.
- Sincronización de estados entre ambos dispositivos.
- Activación remota de la alarma sonora.
- Encendido del LED rojo simulado ante una intrusión.
- Silenciamiento remoto de la sirena.
- Armado y desarmado remoto del sistema.
- Cierre de sesión.

Las pruebas de sincronización y control bidireccional resultaron satisfactorias.

## 8. Alcance y limitaciones

Este proyecto es un prototipo académico.
El sensor LDR, el LED rojo y la sirena se encuentran simulados mediante la aplicación Android. No se utiliza un circuito Arduino físico.
La comunicación depende de la conexión a Internet y de la disponibilidad de Firebase.
El sistema no está destinado a reemplazar una alarma de seguridad comercial.

## 9. Ejecución del proyecto

1. Clonar o descargar el repositorio.
2. Abrir el proyecto en Android Studio.
3. Sincronizar las dependencias de Gradle.
4. Configurar un proyecto Firebase compatible y proporcionar el archivo `google-services.json` correspondiente, si se utiliza una instancia diferente.
5. Ejecutar la aplicación en un dispositivo Android o emulador.
6. Registrar una cuenta o iniciar sesión.
7. Para probar la sincronización, ejecutar la aplicación en dos dispositivos con la misma cuenta de Firebase.

---

**Asignatura:** Aplicaciones Móviles para IoT (TI3042)  
**Proyecto:** Alarma IoT  
**Plataforma:** Android  
**Lenguaje:** Kotlin
