/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.configuration;

/**
 * States for connection to remote components.
 */
public class RemoteServerState {

    public enum State {
        NOT_CONNECTED, OK, ERROR, NO_CREDIT;
    }

    private State actualState;
    private String email;
    private String realName;
    private Long credit;
    private Boolean emailConfirmed;
    private String errorMessage;

    public RemoteServerState(final State actualState, final String email, final String realName, final Long credit,
            final Boolean emailConfirmed) {
        this.actualState = actualState;
        this.email = email;
        this.realName = realName;
        this.credit = credit;
        this.emailConfirmed = emailConfirmed;
    }

    public RemoteServerState(final State actualState, final String email, String errorMessage) {
        this.actualState = actualState;
        this.email = email;
        this.errorMessage = errorMessage;
    }

    public State getActualState() {
        return actualState;
    }

    public String getEmail() {
        return email;
    }

    public String getRealName() {
        return realName;
    }

    public Long getCredit() {
        return credit;
    }

    public Boolean isEmailConfirmed() {
        return emailConfirmed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
