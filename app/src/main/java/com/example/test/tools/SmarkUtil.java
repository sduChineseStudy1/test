package com.example.test.tools;

import com.example.test.MainActivity;
import com.example.test.Practice.Flags;
import com.example.test.pojo.userInform;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.util.TLSUtils;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.pubsub.AccessModel;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.pubsub.SubscribeForm;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.xdata.packet.DataForm;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;

import javax.net.ssl.SSLContext;

public class SmarkUtil {
    private XMPPTCPConnection connection;
    private String userName;
    private String password;
    private String xmppDomain;
    private String serverIP;

    public SmarkUtil(String userName, String password, String xmppDomain, String serverIP) {
        this.userName = userName;
        this.password = password;
        this.xmppDomain = xmppDomain;
        this.serverIP = serverIP;
        try {
            if (connection == null) {
                getConnection(userName, password, xmppDomain, serverIP);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ????????????
     *
     * @param userName
     * @param password
     * @return
     * @throws Exception
     */
    public void getConnection(String userName, String password, String xmppDomain, String serverName) throws Exception {
        try {
            System.out.println("????????????Chat???????????????");
            XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder()
                    .setHostAddress(InetAddress.getByName(serverName))
                    .setUsernameAndPassword(userName, password)
                    .setXmppDomain(xmppDomain)
                    .setSendPresence(false)
                    .setCompressionEnabled(true)
                    .setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.ifpossible);
            //TLS????????????
            XMPPTCPConnectionConfiguration mconfigBuilder = TLSUtils.acceptAllCertificates(TLSUtils.disableHostnameVerificationForTlsCertificates(configBuilder)).build();
            connection = new XMPPTCPConnection(mconfigBuilder);
            // ???????????????
            connection.connect();
            // ???????????????
            connection.login();
            System.out.println("????????????");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * ??????????????????
     */
    public void setState(Presence presence) throws Exception {
        if (connection != null)
            connection.sendStanza(presence);
    }

    /**
     * ??????????????????
     *
     * @param path
     * @return
     * @throws Exception
     */
    public List<Message> getOfflineMessage(String path) throws Exception {
        OfflineMessageManager offlineManager = new OfflineMessageManager(connection);
        //??????????????????
        List<Message> messageList = offlineManager.getMessages();

        //?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        int count = offlineManager.getMessageCount();
        if (count < 1) {
            //????????????????????????????????????????????????????????????????????????????????????????????????
            Presence presence = new Presence(Presence.Type.available);
            connection.sendStanza(presence);
            return null;
        }
        JsonUtil jsonUtil = new JsonUtil();
        for (Message message : messageList) {
            // ????????????
            System.out.println("??????????????????" + message.getFrom() + ":" + message.getType() + ";" + message.getBody());
            String from = message.getFrom().toString().split("/")[0];
            String storagePath = path + File.separator + from + ".json";
            jsonUtil.write(storagePath, from, from.split("@")[0], message.getBody(), "1");
            String StatePath = path + File.separator + "userState.json";
            jsonUtil.writeUserMsgState(StatePath, from.toString(), Flags.notRead);
        }
        //?????????????????????????????????
        offlineManager.deleteMessages();
        //????????????????????????????????????????????????????????????????????????????????????????????????
        Presence presence = new Presence(Presence.Type.available);
        connection.sendStanza(presence);
        return messageList;
    }

    /**
     * ?????????????????????
     *
     * @param rosterListener
     */
    public void addRosterListener(RosterListener rosterListener) {
        Roster roster = Roster.getInstanceFor(connection);
        roster.addRosterListener(rosterListener);
    }

    /**
     * ?????????????????????
     *
     * @param userName ?????????
     * @param password ??????
     * @param attr     ????????????
     * @return
     * @throws Exception
     */
    public boolean registerAccount(String userName, String password, Map<String, String> attr) throws Exception {
        AccountManager manager = AccountManager.getInstance(connection);
        manager.sensitiveOperationOverInsecureConnection(true);
        Localpart l_username = Localpart.from(userName);
        if (attr == null) {
            manager.createAccount(l_username, password);
        } else {
            manager.createAccount(l_username, password, attr);
        }
        return true;
    }

    /**
     * ??????????????????????????????
     *
     * @param password
     * @return
     * @throws Exception
     */
    public boolean changePassword(String password) throws Exception {
        AccountManager manager = AccountManager.getInstance(connection);
        manager.sensitiveOperationOverInsecureConnection(true);
        manager.changePassword(password);
        return true;
    }

    /**
     * ????????????????????????
     *
     * @return
     * @throws Exception
     */
    public boolean deleteAccount() throws Exception {
        AccountManager manager = AccountManager.getInstance(connection);
        manager.sensitiveOperationOverInsecureConnection(true);
        manager.deleteAccount();
        return true;
    }

    /**
     * ????????????????????????
     *
     * @return
     * @throws Exception
     */
    public List getAccountInfo() throws Exception {
        List<String> list = new ArrayList<String>();
        AccountManager manager = AccountManager.getInstance(connection);
        manager.sensitiveOperationOverInsecureConnection(true);
        Set<String> set = manager.getAccountAttributes();
        list.addAll(set);
        return list;
    }

    /**
     * ???????????????
     *
     * @return
     * @throws Exception
     */
    public List<RosterGroup> getGroups() throws Exception {
        List<RosterGroup> grouplist = new ArrayList<RosterGroup>();
        Roster roster = Roster.getInstanceFor(connection);
        Collection<RosterGroup> rosterGroup = roster.getGroups();
        Iterator<RosterGroup> i = rosterGroup.iterator();
        while (i.hasNext()) {
            grouplist.add(i.next());
        }
        return grouplist;
    }

    /**
     * ????????????
     *
     * @param groupName
     * @return
     * @throws Exception
     */
    public boolean addGroup(String groupName) throws Exception {
        Roster roster = Roster.getInstanceFor(connection);
        roster.createGroup(groupName);
        return true;
    }

    /**
     * ???????????????????????????
     *
     * @param groupName
     * @return
     * @throws Exception
     */
    public List<RosterEntry> getEntriesByGroup(String groupName) throws Exception {
        Roster roster = Roster.getInstanceFor(connection);
        List<RosterEntry> Entrieslist = new ArrayList<RosterEntry>();
        RosterGroup rosterGroup = roster.getGroup(groupName);
        Collection<RosterEntry> rosterEntry = rosterGroup.getEntries();
        Iterator<RosterEntry> i = rosterEntry.iterator();
        while (i.hasNext()) {
            Entrieslist.add(i.next());
        }
        return Entrieslist;
    }

    /**
     * ??????????????????
     *
     * @return
     * @throws Exception
     */
    public List<RosterEntry> getAllEntries(List<RosterEntry> entries) throws Exception {
        Roster roster = Roster.getInstanceFor(connection);
        if (!roster.isLoaded())
            roster.reloadAndWait();
        Collection<RosterEntry> rosterEntry = roster.getEntries();
        for (RosterEntry entry : rosterEntry) {
            entries.add(entry);
        }
        return entries;
    }

    /**
     * ????????????VCard??????
     *
     * @param userName
     * @return
     * @throws Exception
     */
    public VCard getUserVCard(String userName) throws Exception {
        VCard vcard = new VCard();
        EntityBareJid jid = JidCreate.entityBareFrom(userName + "@" + this.serverIP);
        vcard.load(connection, jid);
        return vcard;
    }

    /**
     * ???????????? ?????????
     *
     * @param userName
     * @param name
     * @return
     * @throws Exception
     */
    public boolean addUser(String userName, String name) throws Exception {
        Roster roster = Roster.getInstanceFor(connection);
        EntityBareJid jid = JidCreate.entityBareFrom(userName + "@" + this.xmppDomain);
        roster.createEntry(jid, name, null);
        return true;
    }

    /**
     * ???????????? ?????????
     *
     * @param userName
     * @param name
     * @param groupName
     * @return
     * @throws Exception
     */
    public boolean addUser(String userName, String name, String groupName) throws Exception {
        Roster roster = Roster.getInstanceFor(connection);
        EntityBareJid jid = JidCreate.entityBareFrom(userName + "@" + this.xmppDomain);
        roster.createEntry(jid, name, new String[]{groupName});
        Presence presence = new Presence(Presence.Type.subscribed);
        presence.setTo(jid);
        connection.sendStanza(presence);
        return true;
    }


    /**
     * ??????JID???????????????????????????
     *
     * @param JID
     * @return
     * @throws XmppStringprepException
     */
    public boolean getUserByJid(String JID) throws XmppStringprepException {
        Roster roster = Roster.getInstanceFor(connection);
        EntityBareJid jid = JidCreate.entityBareFrom(JID);
        RosterEntry entry = roster.getEntry(jid);
        if (entry == null) return false;
        return true;
    }

    /**
     * ????????????
     *
     * @param userName
     * @return
     * @throws Exception
     */
    public boolean removeUser(String userName) throws Exception {
        Roster roster = Roster.getInstanceFor(connection);
        EntityBareJid jid = JidCreate.entityBareFrom(userName + "@" + this.xmppDomain);
        RosterEntry entry = roster.getEntry(jid);
        System.out.println("???????????????" + userName);
        System.out.println("User." + roster.getEntry(jid) == null);
        roster.removeEntry(entry);
        return true;
    }

    /**
     * ????????????????????????
     *
     * @param nodeId
     * @return
     * @throws Exception
     */
    public boolean createPubSubNode(String nodeId) throws Exception {
        PubSubManager mgr = PubSubManager.getInstance(connection);
        // Create the node
        LeafNode leaf = mgr.createNode(nodeId);
        ConfigureForm form = new ConfigureForm(DataForm.Type.submit);
        form.setAccessModel(AccessModel.open);
        form.setDeliverPayloads(true);
        form.setNotifyRetract(true);
        form.setPersistentItems(true);
        form.setPublishModel(PublishModel.open);
        form.setMaxItems(10000000);// ????????????????????????????????????
        leaf.sendConfigurationForm(form);
        return true;
    }

    /**
     * ????????????????????????
     *
     * @param nodeId
     * @param title
     * @return
     * @throws Exception
     */
    public boolean createPubSubNode(String nodeId, String title) throws Exception {
        PubSubManager mgr = PubSubManager.getInstance(connection);
        // Create the node
        LeafNode leaf = mgr.createNode(nodeId);
        ConfigureForm form = new ConfigureForm(DataForm.Type.submit);
        form.setAccessModel(AccessModel.open);
        form.setDeliverPayloads(true);
        form.setNotifyRetract(true);
        form.setPersistentItems(true);
        form.setPublishModel(PublishModel.open);
        form.setTitle(title);
        form.setBodyXSLT(nodeId);
        form.setMaxItems(10000000);// ????????????????????????????????????
        form.setMaxPayloadSize(1024 * 12);//?????????????????????????????????
        leaf.sendConfigurationForm(form);
        return true;
    }

    public boolean deletePubSubNode(String nodeId) throws Exception {
        PubSubManager mgr = PubSubManager.getInstance(connection);
        mgr.deleteNode(nodeId);
        return true;
    }

    /**
     * ????????????
     *
     * @param nodeId         ??????ID
     * @param eventId        ??????ID
     * @param messageType    ???????????????publish????????????/receipt????????????/state????????????
     * @param messageLevel   0/1/2
     * @param messageSource  ????????????
     * @param messageCount   ????????????
     * @param packageCount   ?????????
     * @param packageNumber  ????????????
     * @param createTime     ???????????? 2018-06-07 09:43:06
     * @param messageContent ????????????
     * @return
     * @throws Exception
     */
    public boolean publish(String nodeId, String eventId, String messageType, int messageLevel, String messageSource,
                           int messageCount, int packageCount, int packageNumber, String createTime, String messageContent)
            throws Exception {
        if (messageContent.length() > 1024 * 10) {
            throw new Exception("????????????????????????1024*10???????????????????????????");
        }
        PubSubManager mgr = PubSubManager.getInstance(connection);
        LeafNode node = null;
        node = mgr.getNode(nodeId);
        StringBuffer xml = new StringBuffer();
        xml.append("<pubmessage xmlns='pub:message'>");
        xml.append("<nodeId>" + nodeId + "</nodeId>");
        xml.append("<eventId>" + eventId + "</eventId>");
        xml.append("<messageType>" + messageType + "</messageType>");
        xml.append("<messageLevel>" + messageLevel + "</messageLevel>");
        xml.append("<messageSource>" + messageSource + "</messageSource>");
        xml.append("<messageCount>" + messageCount + "</messageCount>");
        xml.append("<packageCount>" + packageCount + "</packageCount>");
        xml.append("<packageNumber>" + packageNumber + "</packageNumber>");
        xml.append("<createTime>" + createTime + "</createTime>");
        xml.append("<messageContent>" + messageContent + "</messageContent>");
        xml.append("</pubmessage>");
        SimplePayload payload = new SimplePayload("pubmessage", "pub:message", xml.toString().toLowerCase());
        PayloadItem<SimplePayload> item = new PayloadItem<SimplePayload>(System.currentTimeMillis() + "", payload);
        node.publish(item);
        return true;
    }

    /**
     * ????????????
     *
     * @param nodeId
     * @return
     * @throws Exception
     */
    public boolean subscribe(String nodeId) throws Exception {
        PubSubManager mgr = PubSubManager.getInstance(connection);
        // Get the node
        LeafNode node = mgr.getNode(nodeId);
        SubscribeForm subscriptionForm = new SubscribeForm(DataForm.Type.submit);
        subscriptionForm.setDeliverOn(true);
        subscriptionForm.setDigestFrequency(5000);
        subscriptionForm.setDigestOn(true);
        subscriptionForm.setIncludeBody(true);
        List<Subscription> subscriptions = node.getSubscriptions();
        boolean flag = true;
        for (Subscription s : subscriptions) {
            if (s.getJid().toString().equalsIgnoreCase(connection.getUser().asEntityBareJidString())) {// ????????????
                flag = false;
                break;
            }
        }
        if (flag) {// ????????????????????????
            node.subscribe(userName + "@" + this.serverIP, subscriptionForm);
        }
        return true;
    }

    /**
     * ???????????????????????????
     *
     * @return
     * @throws Exception
     */
    public List<Subscription> querySubscriptions() throws Exception {
        PubSubManager mgr = PubSubManager.getInstance(connection);
        List<Subscription> subs = mgr.getSubscriptions();
        return subs;
    }

    /**
     * ?????????????????????????????????
     *
     * @param nodeId
     * @return
     * @throws Exception
     */
    public ConfigureForm getConfig(String nodeId) throws Exception {
        PubSubManager mgr = PubSubManager.getInstance(connection);
        LeafNode node = mgr.getNode(nodeId);
        ConfigureForm config = node.getNodeConfiguration();
        return config;
    }

    /**
     * ???????????????????????????????????????
     *
     * @return
     * @throws Exception
     */
    public List<Item> queryHistoryMeassage() throws Exception {
        List<Item> result = new ArrayList<Item>();
        PubSubManager mgr = PubSubManager.getInstance(connection);
        List<Subscription> subs = mgr.getSubscriptions();
        if (subs != null && subs.size() > 0) {
            for (Subscription sub : subs) {
                String nodeId = sub.getNode();
                LeafNode node = mgr.getNode(nodeId);
                List<Item> list = node.getItems();
                result.addAll(list);
            }
        }
        /*
         * for (Item item : result) { System.out.println(item.toXML()); }
         */
        return result;
    }

    /**
     * ???????????????????????????????????????
     *
     * @return
     * @throws Exception
     */
    public List<Item> queryHistoryMeassage(String nodeId) throws Exception {
        List<Item> result = new ArrayList<Item>();
        PubSubManager mgr = PubSubManager.getInstance(connection);
        LeafNode node = mgr.getNode(nodeId);
        List<Item> list = node.getItems();
        result.addAll(list);
        /*
         * for (Item item : result) { System.out.println(item.toXML()); }
         */
        return result;
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param nodeId
     * @param num
     * @return
     * @throws Exception
     */
    public List<Item> queryHistoryMeassage(String nodeId, int num) throws Exception {
        List<Item> result = new ArrayList<Item>();
        PubSubManager mgr = PubSubManager.getInstance(connection);
        LeafNode node = mgr.getNode(nodeId);
        List<Item> list = node.getItems(num);
        result.addAll(list);
        /*
         * for (Item item : result) { System.out.println(item.toXML()); }
         */
        return result;
    }

    /**
     * ???????????????????????????
     *
     * @param JID
     * @param message
     * @throws Exception
     */
    public void sendMessage(String JID, String message) throws Exception {
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        EntityBareJid jid = JidCreate.entityBareFrom(JID);
        Chat chat = chatManager.chatWith(jid);
        Message newMessage = new Message();
        newMessage.setBody(message);
        chat.send(newMessage);
    }

    /**
     * ?????????????????????????????????
     *
     * @param chatManagerListener
     * @throws Exception
     */
    public void addOutgoingChatMessageListener(OutgoingChatMessageListener chatManagerListener) throws Exception {
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addOutgoingListener(chatManagerListener);
    }

    /**
     * ?????????????????????????????????
     *
     * @param chatManagerListener
     * @throws Exception
     */
    public void removeIncomingChatMessageListener(IncomingChatMessageListener chatManagerListener) throws Exception {
        org.jivesoftware.smack.chat2.ChatManager chatManager = ChatManager.getInstanceFor(connection);
        chatManager.removeIncomingListener(chatManagerListener);
    }

    /**
     * ?????????????????????????????????
     *
     * @param chatManagerListener
     * @throws Exception
     */
    public void addIncomingChatMessageListener(IncomingChatMessageListener chatManagerListener) throws Exception {
        org.jivesoftware.smack.chat2.ChatManager chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addIncomingListener(chatManagerListener);
    }

    /**
     * ???????????????????????????????????????JID???UerName????????????????????????userInform
     *
     * @param searchName ??????????????????
     * @param items      ??????????????????
     * @return
     * @throws XmppStringprepException
     * @throws XMPPException.XMPPErrorException
     * @throws SmackException.NotConnectedException
     * @throws SmackException.NoResponseException
     * @throws InterruptedException
     */
    public List<userInform> searchEntries(String searchName, List<userInform> items) throws XmppStringprepException, XMPPException.XMPPErrorException, SmackException.NotConnectedException, SmackException.NoResponseException, InterruptedException {
        UserSearchManager userSearchManager = new UserSearchManager(connection);
        if (connection != null) {
            DomainBareJid domainBareJid = JidCreate.domainBareFrom("search." + connection.getXMPPServiceDomain());
            Form searchForm = userSearchManager.getSearchForm(domainBareJid);
            Form answerForm = searchForm.createAnswerForm();
            answerForm.setAnswer("Username", true);
            answerForm.setAnswer("search", searchName.trim());

            ReportedData result = userSearchManager.getSearchResults(answerForm, domainBareJid);
            List<ReportedData.Row> rows = result.getRows();
            for (ReportedData.Row row : rows) {
                //?????????????????????[$jid$]????????????????????????
                String JID = row.getValues("jid").toString();
                JID = JID.substring(1, JID.length() - 1);
                String name = row.getValues("username").toString();
                name = name.substring(1, name.length() - 1);
                userInform user = new userInform(JID, name);
                items.add(user);
            }
        }
        return items;
    }

    /**
     * ?????????????????????????????????
     *
     * @param jid
     */
    public void addSearchFriend(String jid, String nickName) throws XmppStringprepException, SmackException.NotConnectedException, InterruptedException, XMPPException.XMPPErrorException, SmackException.NoResponseException, SmackException.NotLoggedInException {
        Roster roster = Roster.getInstanceFor(connection);
        roster.createEntry(JidCreate.bareFrom(jid), nickName, new String[]{"Friends"});
        Presence presence = new Presence(Presence.Type.subscribed);
        presence.setTo(jid);
        connection.sendStanza(presence);
    }

    /**
     * ??????stanza
     *
     * @param stanzaListener
     */
    public void addPacketListener(StanzaListener stanzaListener) {
        connection.addSyncStanzaListener(stanzaListener, new StanzaFilter() {
            @Override
            public boolean accept(Stanza stanza) {
                return true;
            }
        });
    }

    /**
     * ??????????????????
     */
    public boolean isConnected() {
        if (connection == null) return false;
        return connection.isConnected();
    }

    /**
     * ????????????
     */
    public void reConnect() throws Exception {
        if (connection == null) return;
        connection.connect();
    }

    /**
     * ????????????
     */
    public void reLogin() throws Exception {
        if (connection == null) return;
        connection.login();
    }

    /**
     * ???????????????????????????
     *
     * @return
     */
    public EntityFullJid getUser() {
        return connection.getUser();
    }

    /**
     * ???????????????????????????Jid
     *
     * @return
     * @throws XmppStringprepException
     */
    public BareJid getJid() throws XmppStringprepException {
        return JidCreate.bareFrom(userName + "@" + xmppDomain);
    }

    /**
     * ??????????????????
     */
    public boolean isLogin() {
        if (connection == null) return false;
        return connection.isAuthenticated();
    }

    /**
     * ????????????
     */
    public void close() throws Exception {
        Presence presence = new Presence(Presence.Type.unavailable);
        connection.sendStanza(presence);
        connection.disconnect();
        connection = null;
    }
}