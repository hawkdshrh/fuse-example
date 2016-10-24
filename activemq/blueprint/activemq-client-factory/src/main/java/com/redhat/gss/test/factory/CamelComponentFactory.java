/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.gss.test.factory;

import org.apache.camel.Component;

/**
 *
 * @author dhawkins
 */
public interface CamelComponentFactory {
    
    public Component createComponent();
    
}
