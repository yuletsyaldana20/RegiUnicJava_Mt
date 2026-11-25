package com.regiunic.core.concurrent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Pruebas de Concurrencia y Despacho Asíncrono (AsyncExecutor)")
class AsyncExecutorTest {

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisplayName("runAsync debe ejecutar una tarea en un hilo secundario sin bloquear")
    void testRunAsyncExecutesOnBackgroundThread() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> threadNameRef = new AtomicReference<>();
        String currentThreadName = Thread.currentThread().getName();

        AsyncExecutor.runAsync(() -> {
            threadNameRef.set(Thread.currentThread().getName());
            latch.countDown();
        });

        boolean completed = latch.await(4, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        assertThat(threadNameRef.get()).isNotNull();
        // Debe ejecutarse fuera del hilo de la prueba
        assertThat(threadNameRef.get()).isNotEqualTo(currentThreadName);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisplayName("supplyAsync debe retornar el resultado al callback onSuccess")
    void testSupplyAsyncSuccess() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> resultRef = new AtomicReference<>();
        AtomicBoolean errorCalled = new AtomicBoolean(false);

        AsyncExecutor.supplyAsync(
                () -> "Resultado Calculado",
                res -> {
                    resultRef.set(res);
                    latch.countDown();
                },
                err -> {
                    errorCalled.set(true);
                    latch.countDown();
                }
        );

        boolean completed = latch.await(4, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        assertThat(resultRef.get()).isEqualTo("Resultado Calculado");
        assertThat(errorCalled.get()).isFalse();
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @DisplayName("supplyAsync debe capturar la excepción y redirigirla a onError")
    void testSupplyAsyncFailure() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();
        AtomicReference<String> successRef = new AtomicReference<>();

        AsyncExecutor.<String>supplyAsync(
                () -> {
                    throw new IllegalStateException("Error simulado de base de datos");
                },
                res -> {
                    successRef.set(res);
                    latch.countDown();
                },
                err -> {
                    errorRef.set(err);
                    latch.countDown();
                }
        );

        boolean completed = latch.await(4, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        assertThat(successRef.get()).isNull();
        assertThat(errorRef.get())
                .isNotNull()
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Error simulado de base de datos");
    }
}
