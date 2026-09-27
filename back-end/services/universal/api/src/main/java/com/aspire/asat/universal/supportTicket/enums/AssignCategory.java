package com.aspire.asat.universal.supportTicket.enums;

/**
 * Enum to categorize support tickets based on assignment hierarchy.
 * 
 * - ASSIGNTOCLIENT: Ticket assigned to client admin (created by end user)
 * - ASSIGNTOMSP: Ticket assigned to MSP admin (created by client admin)
 * - ASSIGNTOSUPER: Ticket assigned to super admin (created by MSP admin)
 */
public enum AssignCategory {
    /**
     * Ticket assigned to client admin
     * Created by: End User
     * Assigned to: Client Admin
     */
    ASSIGNTOCLIENT,
    
    /**
     * Ticket assigned to MSP admin
     * Created by: Client Admin
     * Assigned to: MSP Admin
     */
    ASSIGNTOMSP,
    
    /**
     * Ticket assigned to super admin
     * Created by: MSP Admin
     * Assigned to: Super Admin
     */
    ASSIGNTOSUPER
}

