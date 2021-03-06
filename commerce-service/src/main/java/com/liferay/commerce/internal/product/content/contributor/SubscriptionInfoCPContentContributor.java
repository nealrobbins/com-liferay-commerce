/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.commerce.internal.product.content.contributor;

import com.liferay.commerce.product.constants.CPContentContributorConstants;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.model.CPSubscriptionInfo;
import com.liferay.commerce.product.util.CPContentContributor;
import com.liferay.commerce.product.util.CPSubscriptionType;
import com.liferay.commerce.product.util.CPSubscriptionTypeRegistry;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alessio Antonio Rendina
 */
@Component(
	immediate = true,
	property = "commerce.product.content.contributor.name=" + CPContentContributorConstants.SUBSCRIPTION_INFO,
	service = CPContentContributor.class
)
public class SubscriptionInfoCPContentContributor
	implements CPContentContributor {

	@Override
	public String getName() {
		return CPContentContributorConstants.SUBSCRIPTION_INFO;
	}

	@Override
	public JSONObject getValue(
			CPInstance cpInstance, HttpServletRequest httpServletRequest)
		throws PortalException {

		JSONObject jsonObject = _jsonFactory.createJSONObject();

		if (cpInstance == null) {
			return jsonObject;
		}

		CPSubscriptionInfo cpSubscriptionInfo =
			cpInstance.getCPSubscriptionInfo();

		if (cpSubscriptionInfo == null) {
			jsonObject.put(
				CPContentContributorConstants.SUBSCRIPTION_INFO,
				StringPool.BLANK);

			return jsonObject;
		}

		String subscriptionInfo = _getSubscriptionInfo(
			cpSubscriptionInfo, httpServletRequest);

		jsonObject.put(
			CPContentContributorConstants.SUBSCRIPTION_INFO, subscriptionInfo);

		return jsonObject;
	}

	private String _getSubscriptionInfo(
		CPSubscriptionInfo cpSubscriptionInfo,
		HttpServletRequest httpServletRequest) {

		long maxSubscriptionCycles =
			cpSubscriptionInfo.getMaxSubscriptionCycles();
		int subscriptionLength = cpSubscriptionInfo.getSubscriptionLength();

		String period = StringPool.BLANK;

		CPSubscriptionType cpSubscriptionType =
			_cpSubscriptionTypeRegistry.getCPSubscriptionType(
				cpSubscriptionInfo.getSubscriptionType());

		if (cpSubscriptionType != null) {
			period = cpSubscriptionType.getLabel(
				_portal.getLocale(httpServletRequest));
		}

		StringBundler sb = new StringBundler(
			(maxSubscriptionCycles > 0) ? 11 : 7);

		sb.append(StringPool.OPEN_PARENTHESIS);
		sb.append(LanguageUtil.get(httpServletRequest, "every"));
		sb.append(StringPool.SPACE);
		sb.append(subscriptionLength);
		sb.append(StringPool.SPACE);
		sb.append(_getSuffix(subscriptionLength, period, httpServletRequest));
		sb.append(StringPool.CLOSE_PARENTHESIS);

		if (maxSubscriptionCycles > 0) {
			long totalLength = subscriptionLength * maxSubscriptionCycles;

			String duration = LanguageUtil.format(
				httpServletRequest, "duration-x", totalLength, false);

			String durationSuffix = _getSuffix(
				totalLength, period, httpServletRequest);

			sb.append(StringPool.SPACE);
			sb.append(duration);
			sb.append(StringPool.SPACE);
			sb.append(durationSuffix);
		}

		return sb.toString();
	}

	private String _getSuffix(
		long count, String period, HttpServletRequest httpServletRequest) {

		if (count > 1) {
			return LanguageUtil.get(
				httpServletRequest,
				StringUtil.toLowerCase(period) + CharPool.LOWER_CASE_S);
		}

		return period;
	}

	@Reference
	private CPSubscriptionTypeRegistry _cpSubscriptionTypeRegistry;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Portal _portal;

}