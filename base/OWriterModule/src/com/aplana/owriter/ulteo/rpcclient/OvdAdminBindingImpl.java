/**
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to you under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
/**
 * OvdAdminBindingImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.aplana.owriter.ulteo.rpcclient;

public class OvdAdminBindingImpl implements com.aplana.owriter.ulteo.rpcclient.OvdAdminPortType{
    public boolean test_link_connected() throws java.rmi.RemoteException {
        return false;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any getInitialConfiguration() throws java.rmi.RemoteException {
        return null;
    }

    public void system_switch_maintenance(javax.xml.rpc.holders.BooleanHolder parameters) throws java.rmi.RemoteException {
    }

    public boolean administrator_password_set(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any settings_get() throws java.rmi.RemoteException {
        return null;
    }

    public boolean settings_set(com.aplana.owriter.ulteo.rpcclient.Any parameters) throws java.rmi.RemoteException {
        return false;
    }

    public void settings_domain_integration_preview(com.aplana.owriter.ulteo.rpcclient.holders.AnyHolder parameters) throws java.rmi.RemoteException {
    }

    public com.aplana.owriter.ulteo.rpcclient.Any servers_list(java.lang.String parameters) throws java.rmi.RemoteException {
        return null;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any server_info(java.lang.String parameters) throws java.rmi.RemoteException {
        return null;
    }

    public boolean server_remove(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public boolean server_register(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public boolean server_switch_maintenance(java.lang.String server_id, boolean maintenance) throws java.rmi.RemoteException {
        return false;
    }

    public boolean server_set_available_sessions(java.lang.String server_id, java.math.BigInteger nb_session) throws java.rmi.RemoteException {
        return false;
    }

    public boolean server_set_display_name(java.lang.String server_id, java.lang.String display_name) throws java.rmi.RemoteException {
        return false;
    }

    public boolean server_unset_display_name(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public boolean server_set_fqdn(java.lang.String server_id, java.lang.String fqdn) throws java.rmi.RemoteException {
        return false;
    }

    public boolean server_set_external_name(java.lang.String server_id, java.lang.String external_name) throws java.rmi.RemoteException {
        return false;
    }

    public boolean server_unset_external_name(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public boolean server_set_rdp_port(java.lang.String server_id, java.math.BigInteger rdp_port) throws java.rmi.RemoteException {
        return false;
    }

    public boolean server_unset_rdp_port(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public boolean server_role_enable(java.lang.String server_id, java.lang.String role) throws java.rmi.RemoteException {
        return false;
    }

    public boolean server_role_disable(java.lang.String server_id, java.lang.String role) throws java.rmi.RemoteException {
        return false;
    }

    public boolean server_add_static_application(java.lang.String application, java.lang.String server) throws java.rmi.RemoteException {
        return false;
    }

    public boolean server_remove_static_application(java.lang.String application, java.lang.String server) throws java.rmi.RemoteException {
        return false;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any servers_groups_list() throws java.rmi.RemoteException {
        return null;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any servers_group_info(java.lang.String parameters) throws java.rmi.RemoteException {
        return null;
    }

    public java.lang.String servers_group_add(java.lang.String name, java.lang.String description) throws java.rmi.RemoteException {
        return null;
    }

    public boolean servers_group_remove(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public boolean servers_group_modify(java.lang.String id, java.lang.String name, java.lang.String description, boolean published) throws java.rmi.RemoteException {
        return false;
    }

    public boolean servers_group_add_server(java.lang.String server, java.lang.String group) throws java.rmi.RemoteException {
        return false;
    }

    public boolean servers_group_remove_server(java.lang.String server, java.lang.String group) throws java.rmi.RemoteException {
        return false;
    }

    public boolean servers_group_publication_add(java.lang.String server, java.lang.String group) throws java.rmi.RemoteException {
        return false;
    }

    public boolean servers_group_publication_remove(java.lang.String server, java.lang.String group) throws java.rmi.RemoteException {
        return false;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any tasks_list() throws java.rmi.RemoteException {
        return null;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any task_info(java.lang.String parameters) throws java.rmi.RemoteException {
        return null;
    }

    public boolean task_remove(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public java.lang.String task_debian_install_packages(java.lang.String first, java.lang.String second) throws java.rmi.RemoteException {
        return null;
    }

    public java.lang.String task_debian_installable_application(java.lang.String first, java.lang.String second) throws java.rmi.RemoteException {
        return null;
    }

    public void task_debian_upgrade(javax.xml.rpc.holders.StringHolder parameters) throws java.rmi.RemoteException {
    }

    public boolean task_debian_server_replicate(java.lang.String first, java.lang.String second) throws java.rmi.RemoteException {
        return false;
    }

    public boolean task_debian_application_install(java.lang.String application, java.lang.String server) throws java.rmi.RemoteException {
        return false;
    }

    public boolean task_debian_application_remove(java.lang.String application, java.lang.String server) throws java.rmi.RemoteException {
        return false;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any applications_list(java.lang.String parameters) throws java.rmi.RemoteException {
        return null;
    }

    public com.aplana.owriter.ulteo.rpcclient.Application application_info(java.lang.String parameters) throws java.rmi.RemoteException {
        return null;
    }

    public boolean application_remove(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public boolean application_publish(java.lang.String application, boolean publish) throws java.rmi.RemoteException {
        return false;
    }

    public boolean applications_remove_orphans() throws java.rmi.RemoteException {
        return false;
    }

    public boolean application_clone(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public void application_icon_get(javax.xml.rpc.holders.StringHolder parameters) throws java.rmi.RemoteException {
    }

    public boolean application_icon_set(java.lang.String application, java.lang.String icon) throws java.rmi.RemoteException {
        return false;
    }

    public java.lang.String application_icon_getFromServer(java.lang.String application, java.lang.String server) throws java.rmi.RemoteException {
        return null;
    }

    public boolean application_icon_setFromServer(java.lang.String application, java.lang.String server) throws java.rmi.RemoteException {
        return false;
    }

    public java.lang.String application_static_add(java.lang.String name, java.lang.String description, java.lang.String type, java.lang.String command) throws java.rmi.RemoteException {
        return null;
    }

    public boolean application_static_remove(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public boolean application_static_removeIcon(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public java.lang.String application_webapp_add(java.lang.String name, java.lang.String description, java.lang.String url_prefix, com.aplana.owriter.ulteo.rpcclient.Any configuration) throws java.rmi.RemoteException {
        return null;
    }

    public com.aplana.owriter.ulteo.rpcclient.Web_application application_webapp_info(java.math.BigInteger application_id) throws java.rmi.RemoteException {
        return null;
    }

    public boolean application_webapp_modify(java.math.BigInteger application_id, com.aplana.owriter.ulteo.rpcclient.Web_application parameters) throws java.rmi.RemoteException {
        return false;
    }

    public boolean application_webapp_set_values(java.math.BigInteger application_id, com.aplana.owriter.ulteo.rpcclient.Any values) throws java.rmi.RemoteException {
        return false;
    }

    public boolean application_webapp_clone(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public boolean application_webapp_remove(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public boolean application_static_modify(java.lang.String id, java.lang.String name, java.lang.String description, java.lang.String command) throws java.rmi.RemoteException {
        return false;
    }

    public java.lang.String application_weblink_add(java.lang.String name, java.lang.String description, java.lang.String url) throws java.rmi.RemoteException {
        return null;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any default_browser_get() throws java.rmi.RemoteException {
        return null;
    }

    public boolean default_browser_set(java.lang.String first, java.lang.String second) throws java.rmi.RemoteException {
        return false;
    }

    public boolean undefault_browser_set(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any applications_groups_list() throws java.rmi.RemoteException {
        return null;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any applications_group_info(java.lang.String parameters) throws java.rmi.RemoteException {
        return null;
    }

    public java.lang.String applications_group_add(java.lang.String name, java.lang.String description) throws java.rmi.RemoteException {
        return null;
    }

    public boolean applications_group_remove(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public boolean applications_group_modify(java.lang.String id, java.lang.String name, java.lang.String description, boolean published) throws java.rmi.RemoteException {
        return false;
    }

    public boolean applications_group_add_application(java.lang.String application, java.lang.String group) throws java.rmi.RemoteException {
        return false;
    }

    public boolean applications_group_remove_application(java.lang.String application, java.lang.String group) throws java.rmi.RemoteException {
        return false;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any mime_types_list() throws java.rmi.RemoteException {
        return null;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any mime_type_info(java.lang.String parameters) throws java.rmi.RemoteException {
        return null;
    }

    public boolean application_add_mime_type(java.lang.String application, java.lang.String mime_type) throws java.rmi.RemoteException {
        return false;
    }

    public boolean applications_remove_mime_type(java.lang.String application, java.lang.String mime_type) throws java.rmi.RemoteException {
        return false;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any scripts_groups_list(java.lang.String parameters) throws java.rmi.RemoteException {
        return null;
    }

    public void users_list_partial(java.lang.String search, java.lang.String[] search_fields, java.lang.String search_user, com.aplana.owriter.ulteo.rpcclient.holders.AnyHolder data, javax.xml.rpc.holders.BooleanHolder partial) throws java.rmi.RemoteException {
        data.value = new com.aplana.owriter.ulteo.rpcclient.Any();
        partial.value = true;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any user_info(java.lang.String parameters) throws java.rmi.RemoteException {
        return null;
    }

    public boolean user_add(java.lang.String login, java.lang.String displayname, java.lang.String password) throws java.rmi.RemoteException {
        return false;
    }

    public boolean user_remove(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public boolean user_modify(java.lang.String login, java.lang.String displayname, java.lang.String password) throws java.rmi.RemoteException {
        return false;
    }

    public boolean users_populate(java.lang.String first, java.lang.String second) throws java.rmi.RemoteException {
        return false;
    }

    public boolean user_settings_set(java.lang.String user_id, java.lang.String container, java.lang.String setting, com.aplana.owriter.ulteo.rpcclient.Any value) throws java.rmi.RemoteException {
        return false;
    }

    public boolean user_settings_remove(java.lang.String user_id, java.lang.String container, java.lang.String setting) throws java.rmi.RemoteException {
        return false;
    }

    public void users_groups_list_partial(java.lang.String search, java.lang.String[] search_fields, java.lang.String search_user, com.aplana.owriter.ulteo.rpcclient.holders.AnyHolder data, javax.xml.rpc.holders.BooleanHolder partial) throws java.rmi.RemoteException {
        data.value = new com.aplana.owriter.ulteo.rpcclient.Any();
        partial.value = true;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any users_group_info(java.lang.String parameters) throws java.rmi.RemoteException {
        return null;
    }

    public java.lang.String users_group_add(java.lang.String name, java.lang.String description) throws java.rmi.RemoteException {
        return null;
    }

    public boolean users_group_remove(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public boolean users_group_modify(java.lang.String id, java.lang.String name, java.lang.String description, boolean published) throws java.rmi.RemoteException {
        return false;
    }

    public boolean system_set_default_users_group(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public boolean system_unset_default_users_group() throws java.rmi.RemoteException {
        return false;
    }

    public java.lang.String users_group_dynamic_add(java.lang.String name, java.lang.String description, java.lang.String validation_type) throws java.rmi.RemoteException {
        return null;
    }

    public boolean users_group_dynamic_modify(java.lang.String id, com.aplana.owriter.ulteo.rpcclient.Any rules, java.lang.String validation_type) throws java.rmi.RemoteException {
        return false;
    }

    public boolean users_group_dynamic_cached_add(java.lang.String name, java.lang.String description, java.lang.String validation_type, java.math.BigInteger schedule) throws java.rmi.RemoteException {
        return false;
    }

    public boolean users_group_dynamic_cached_set_schedule(java.lang.String id, java.math.BigInteger schedule) throws java.rmi.RemoteException {
        return false;
    }

    public boolean users_group_settings_set(java.lang.String group_id, java.lang.String container, java.lang.String setting, com.aplana.owriter.ulteo.rpcclient.Any value) throws java.rmi.RemoteException {
        return false;
    }

    public boolean users_group_settings_remove(java.lang.String group_id, java.lang.String container, java.lang.String setting) throws java.rmi.RemoteException {
        return false;
    }

    public boolean users_group_add_policy(java.lang.String group_id, java.lang.String rule) throws java.rmi.RemoteException {
        return false;
    }

    public boolean users_group_remove_policy(java.lang.String group_id, java.lang.String rule) throws java.rmi.RemoteException {
        return false;
    }

    public boolean users_group_add_user(java.lang.String user_id, java.lang.String group_id) throws java.rmi.RemoteException {
        return false;
    }

    public boolean users_group_remove_user(java.lang.String user_id, java.lang.String group_id) throws java.rmi.RemoteException {
        return false;
    }

    public boolean publication_add(java.lang.String users_group, java.lang.String applications_group) throws java.rmi.RemoteException {
        return false;
    }

    public boolean publication_remove(java.lang.String users_group, java.lang.String applications_group) throws java.rmi.RemoteException {
        return false;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any scripts_list() throws java.rmi.RemoteException {
        return null;
    }

    public boolean script_modify(java.lang.String id, java.lang.String name, java.lang.String os, java.lang.String type, java.lang.String data) throws java.rmi.RemoteException {
        return false;
    }

    public boolean script_add(java.lang.String name, java.lang.String os, java.lang.String type, java.lang.String data) throws java.rmi.RemoteException {
        return false;
    }

    public boolean script_remove(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public boolean users_group_add_script(java.lang.String script, java.lang.String group) throws java.rmi.RemoteException {
        return false;
    }

    public boolean users_group_remove_script(java.lang.String script, java.lang.String group) throws java.rmi.RemoteException {
        return false;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any script_info(java.lang.String parameters) throws java.rmi.RemoteException {
        return null;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any shared_folders_list() throws java.rmi.RemoteException {
        return null;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any shared_folder_info(java.lang.String parameters) throws java.rmi.RemoteException {
        return null;
    }

    public java.lang.String shared_folder_add(java.lang.String first, java.lang.String second) throws java.rmi.RemoteException {
        return null;
    }

    public boolean shared_folder_remove(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public boolean shared_folder_rename(java.lang.String first, java.lang.String second) throws java.rmi.RemoteException {
        return false;
    }

    public boolean shared_folder_add_group(java.lang.String group_id, java.lang.String share_id, java.lang.String mode) throws java.rmi.RemoteException {
        return false;
    }

    public boolean shared_folder_remove_group(java.lang.String group_id, java.lang.String share_id) throws java.rmi.RemoteException {
        return false;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any users_profiles_list() throws java.rmi.RemoteException {
        return null;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any user_profile_info(java.lang.String parameters) throws java.rmi.RemoteException {
        return null;
    }

    public boolean user_profile_add(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public boolean user_profile_remove(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public boolean network_folder_remove(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any sessions_count() throws java.rmi.RemoteException {
        return null;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any sessions_list(java.math.BigInteger parameters) throws java.rmi.RemoteException {
        return null;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any sessions_list_by_server(java.lang.String server, java.math.BigInteger offset) throws java.rmi.RemoteException {
        return null;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any session_info(java.lang.String parameters) throws java.rmi.RemoteException {
        return null;
    }

    public boolean session_kill(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public boolean session_disconnect(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any session_simulate(java.lang.String parameters) throws java.rmi.RemoteException {
        return null;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any has_valid_certificate() throws java.rmi.RemoteException {
        return null;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any certificates_list() throws java.rmi.RemoteException {
        return null;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any certificates_limits() throws java.rmi.RemoteException {
        return null;
    }

    public boolean certificate_del(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public boolean certificate_reset_named_users() throws java.rmi.RemoteException {
        return false;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any log_preview() throws java.rmi.RemoteException {
        return null;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any log_download(java.lang.String parameters) throws java.rmi.RemoteException {
        return null;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any sessions_reports_list(java.math.BigInteger first, java.math.BigInteger second) throws java.rmi.RemoteException {
        return null;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any sessions_reports_list2(java.math.BigInteger start, java.math.BigInteger stop, java.lang.String server) throws java.rmi.RemoteException {
        return null;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any sessions_reports_list3(java.math.BigInteger from, java.math.BigInteger to, java.lang.String user, java.math.BigInteger limit) throws java.rmi.RemoteException {
        return null;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any session_report_info(java.lang.String parameters) throws java.rmi.RemoteException {
        return null;
    }

    public boolean session_report_remove(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any servers_reports_list(java.math.BigInteger first, java.math.BigInteger second) throws java.rmi.RemoteException {
        return null;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any checkup() throws java.rmi.RemoteException {
        return null;
    }

    public boolean cleanup_liaisons() throws java.rmi.RemoteException {
        return false;
    }

    public boolean cleanup_preferences() throws java.rmi.RemoteException {
        return false;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any news_list() throws java.rmi.RemoteException {
        return null;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any news_info(java.lang.String parameters) throws java.rmi.RemoteException {
        return null;
    }

    public boolean news_modify(java.lang.String id, java.lang.String title, java.lang.String content) throws java.rmi.RemoteException {
        return false;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any news_add(java.lang.String title, java.lang.String content) throws java.rmi.RemoteException {
        return null;
    }

    public boolean news_remove(java.lang.String parameters) throws java.rmi.RemoteException {
        return false;
    }

    public com.aplana.owriter.ulteo.rpcclient.Any admin_actions_list(java.math.BigInteger parameters) throws java.rmi.RemoteException {
        return null;
    }

}
