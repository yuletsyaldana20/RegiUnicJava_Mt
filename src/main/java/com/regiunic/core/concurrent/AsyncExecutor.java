package com.regiunic.core.concurrent;

import javax.swing.SwingUtilities;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Gestor de concurrencia basado en Virtual Threads de Java 25.
 * Permite ejecutar tareas pesadas o de I/O (base de datos, hashing) fuera del
 * Event Dispatch Thread (EDT) de Swing, retornando resultados de forma segura a la UI.
 */
public final class AsyncExecutor {
    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private AsyncExecutor() {}

    /**
     * Ejecuta una tarea asíncrona sin esperar retorno en un Virtual Thread.
     */
    public static void runAsync(Runnable task) {
        EXECUTOR.submit(task);
    }

    /**
     * Ejecuta una tarea asíncrona y entrega el resultado o error en el EDT de Swing.
     */
    public static <T> void supplyAsync(
            Supplier<T> task,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError
    ) {
        EXECUTOR.submit(() -> {
            try {
                T result = task.get();
                if (onSuccess != null) {
                    SwingUtilities.invokeLater(() -> onSuccess.accept(result));
                }
            } catch (Throwable ex) {
                if (onError != null) {
                    SwingUtilities.invokeLater(() -> onError.accept(ex));
                }
            }
        });
    }
}
