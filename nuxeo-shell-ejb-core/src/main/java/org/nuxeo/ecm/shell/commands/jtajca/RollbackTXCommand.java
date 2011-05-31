/*
 * (C) Copyright 2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Julien Carsique
 *
 * $Id$
 */

package org.nuxeo.ecm.shell.commands.jtajca;

import javax.naming.NamingException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.shell.CommandLine;
import org.nuxeo.ecm.shell.commands.repository.AbstractCommand;
import org.nuxeo.runtime.transaction.TransactionHelper;
import org.nuxeo.runtime.transaction.TransactionRuntimeException;

/**
 * @author jcarsique
 */
public class RollbackTXCommand extends AbstractCommand {
    private static final Log log = LogFactory.getLog(RollbackTXCommand.class);

    @Override
    public void run(CommandLine cmdLine) throws NamingException,
            SystemException {
        UserTransaction ut;
        try {
            ut = TransactionHelper.lookupUserTransaction();
            ut.rollback();
        } catch (NamingException e) {
            throw e;
        } catch (IllegalStateException e) {
            throw e;
        } catch (SecurityException e) {
            throw e;
        } catch (SystemException e) {
            throw e;
        }
        if (TransactionHelper.isTransactionActiveOrMarkedRollback()) {
            if (!TransactionHelper.setTransactionRollbackOnly()) {
                throw new TransactionRuntimeException(
                        "Couldn't mark transaction as rollback.");
            }
            TransactionHelper.commitOrRollbackTransaction();
        }
        // NamingContextFactory.revertSetAsInitial();
    }

}
