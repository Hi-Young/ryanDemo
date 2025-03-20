package com.bruce.ioc;

import org.springframework.context.annotation.Import;

@Import(ZooImportSelector.class)
public class ConfigB {
}
