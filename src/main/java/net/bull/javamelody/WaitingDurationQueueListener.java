/*
 * Copyright 2008-2019 by Emeric Vernat
 *
 *     This file is part of the Monitoring plugin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.bull.javamelody;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import hudson.Extension;
import hudson.model.Queue.LeftItem;
import hudson.model.Queue.WaitingItem;
import hudson.model.queue.QueueListener;

/**
 * Listener d'entrée et de sortie de la file d'attente pour calculer un indicateur d'attente.
 * @author Emeric Vernat
 */
@Extension
public class WaitingDurationQueueListener extends QueueListener {
	private static final ConcurrentMap<Long, Date> START_TIMES_BY_ID = new ConcurrentHashMap<>();

	/** {@inheritDoc} */
	@Override
	public void onEnterWaiting(WaitingItem wi) {
		START_TIMES_BY_ID.put(wi.getId(), new Date());
	}

	/** {@inheritDoc} */
	@Override
	public void onLeft(LeftItem li) {
		START_TIMES_BY_ID.remove(li.getId());
	}

	static long getWaitingDurationsSum() {
		final long now = System.currentTimeMillis();
		long sum = 0;
		for (final Date date : START_TIMES_BY_ID.values()) {
			// now can be a bit before date
			sum += now - date.getTime();
		}
		return Math.max(sum, 0);
	}
}
