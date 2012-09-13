package org.bimserver.database.actions;

/******************************************************************************
 * Copyright (C) 2009-2012  BIMserver.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/

import org.bimserver.BimServer;
import org.bimserver.database.BimserverDatabaseException;
import org.bimserver.database.DatabaseSession;
import org.bimserver.database.BimserverLockConflictException;
import org.bimserver.database.ObjectIdentifier;
import org.bimserver.models.log.AccessMethod;
import org.bimserver.models.store.ConcreteRevision;
import org.bimserver.models.store.DataObject;
import org.bimserver.models.store.Revision;
import org.bimserver.shared.exceptions.UserException;

public class GetDataObjectByGuidDatabaseAction extends BimDatabaseAction<DataObject>{

	private final String guid;
	private final long roid;
	private final BimServer bimServer;
	private final long currentUoid;

	public GetDataObjectByGuidDatabaseAction(BimServer bimServer, DatabaseSession databaseSession, AccessMethod accessMethod, long roid, String guid, long currentUoid) {
		super(databaseSession, accessMethod);
		this.bimServer = bimServer;
		this.roid = roid;
		this.guid = guid;
		this.currentUoid = currentUoid;
	}
	
	@Override
	public DataObject execute() throws UserException, BimserverLockConflictException, BimserverDatabaseException {
		Revision virtualRevision = getVirtualRevision(roid);
		ObjectIdentifier objectIdentifier = null;
		for (ConcreteRevision concreteRevision : virtualRevision.getConcreteRevisions()) {
			objectIdentifier = getDatabaseSession().getOidOfGuid(guid, concreteRevision.getProject().getId(), concreteRevision.getId());
			if (objectIdentifier != null) {
				long oidOfGuid = objectIdentifier.getOid();
				if (oidOfGuid != -1) {
					break;
				}
			}
		}
		if (objectIdentifier == null) {
			throw new UserException("Guid " + guid + " not found in this revision/project");
		}
		
		return new GetDataObjectByOidDatabaseAction(bimServer, getDatabaseSession(), getAccessMethod(), roid, objectIdentifier.getOid(), objectIdentifier.getCid(), currentUoid).execute();
	}
}