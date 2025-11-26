package com.assessment.inventory.service.implementation;

import com.assessment.inventory.handler.InventoryHandler;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class InventoryHandlerFactory {
    private final ApplicationContext ctx;

    public InventoryHandler getHandler(String type) {
        return ctx.getBean("defaultInventoryHandler", InventoryHandler.class);
    }
}
