import { useState, useEffect } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator, ScrollView,
} from 'react-native';
import { useLocalSearchParams, Stack, useRouter } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../../../constants/colors';
import { customersApi } from '../../../lib/api';
import type { Customer, Contact, Address, CustomerNote } from '../../../types';

function formatDate(dateStr: string): string {
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

const statusLabels: Record<string, string> = {
  active: 'Hoạt động',
  inactive: 'Không hoạt động',
  lead: 'Tiềm năng',
};

const statusColors: Record<string, string> = {
  active: Colors.success,
  inactive: Colors.textSecondary,
  lead: Colors.warning,
};

export default function CustomerDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const router = useRouter();
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [contacts, setContacts] = useState<Contact[]>([]);
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [notes, setNotes] = useState<CustomerNote[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'info' | 'contacts' | 'addresses' | 'notes'>('info');

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    setError(null);
    Promise.all([
      customersApi.getCustomer(id),
      customersApi.getContacts(id),
      customersApi.getAddresses(id),
      customersApi.getNotes(id),
    ])
      .then(([custRes, contactsRes, addrRes, notesRes]) => {
        setCustomer(custRes.data?.data || custRes.data || null);
        setContacts(contactsRes.data?.data?.content || contactsRes.data?.data || contactsRes.data || []);
        setAddresses(addrRes.data?.data?.content || addrRes.data?.data || addrRes.data || []);
        setNotes(notesRes.data?.data?.content || notesRes.data?.data || notesRes.data || []);
      })
      .catch((err: any) => setError(err?.message || 'Không thể tải thông tin khách hàng'))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color={Colors.primary} />
        <Text style={styles.centerText}>Đang tải thông tin...</Text>
      </View>
    );
  }

  if (error) {
    return (
      <View style={styles.center}>
        <Ionicons name="alert-circle-outline" size={48} color={Colors.error} />
        <Text style={[styles.centerText, { color: Colors.error, marginTop: 12 }]}>{error}</Text>
        <TouchableOpacity
          style={styles.retryBtn}
          onPress={() => {
            setLoading(true);
            setError(null);
            Promise.all([
              customersApi.getCustomer(id!),
              customersApi.getContacts(id!),
              customersApi.getAddresses(id!),
              customersApi.getNotes(id!),
            ])
              .then(([custRes, contactsRes, addrRes, notesRes]) => {
                setCustomer(custRes.data?.data || custRes.data || null);
                setContacts(contactsRes.data?.data?.content || contactsRes.data?.data || contactsRes.data || []);
                setAddresses(addrRes.data?.data?.content || addrRes.data?.data || addrRes.data || []);
                setNotes(notesRes.data?.data?.content || notesRes.data?.data || notesRes.data || []);
              })
              .catch((e: any) => setError(e?.message || 'Không thể tải thông tin khách hàng'))
              .finally(() => setLoading(false));
          }}
        >
          <Text style={styles.retryText}>Thử lại</Text>
        </TouchableOpacity>
      </View>
    );
  }

  if (!customer) {
    return (
      <View style={styles.center}>
        <Ionicons name="person-outline" size={48} color={Colors.textSecondary} />
        <Text style={styles.centerText}>Không tìm thấy khách hàng</Text>
      </View>
    );
  }

  const renderInfoTab = () => (
    <View style={styles.tabContent}>
      <View style={styles.infoCard}>
        <Text style={styles.sectionTitle}>Thông tin cơ bản</Text>
        <View style={styles.infoRow}>
          <Ionicons name="mail-outline" size={16} color={Colors.textSecondary} />
          <Text style={styles.infoLabel}>Email:</Text>
          <Text style={styles.infoValue}>{customer.email || '---'}</Text>
        </View>
        <View style={styles.infoRow}>
          <Ionicons name="call-outline" size={16} color={Colors.textSecondary} />
          <Text style={styles.infoLabel}>Điện thoại:</Text>
          <Text style={styles.infoValue}>{customer.phone || '---'}</Text>
        </View>
        <View style={styles.infoRow}>
          <Ionicons name="business-outline" size={16} color={Colors.textSecondary} />
          <Text style={styles.infoLabel}>Công ty:</Text>
          <Text style={styles.infoValue}>{customer.company || '---'}</Text>
        </View>
        <View style={styles.infoRow}>
          <Ionicons name="ticket-outline" size={16} color={Colors.textSecondary} />
          <Text style={styles.infoLabel}>Số phiếu yêu cầu:</Text>
          <Text style={styles.infoValue}>{customer.totalTickets}</Text>
        </View>
        <View style={styles.infoRow}>
          <Ionicons name="calendar-outline" size={16} color={Colors.textSecondary} />
          <Text style={styles.infoLabel}>Liên hệ gần nhất:</Text>
          <Text style={styles.infoValue}>{customer.lastContact ? formatDate(customer.lastContact) : '---'}</Text>
        </View>
      </View>
    </View>
  );

  const renderContactsTab = () => (
    <View style={styles.tabContent}>
      {contacts.length === 0 ? (
        <View style={styles.emptySection}>
          <Ionicons name="people-outline" size={40} color={Colors.textSecondary} />
          <Text style={styles.emptyText}>Chưa có liên hệ</Text>
        </View>
      ) : (
        contacts.map((contact) => (
          <View key={contact.id} style={styles.itemCard}>
            <View style={styles.itemHeader}>
              <Text style={styles.itemName}>{contact.name}</Text>
              {contact.isPrimary && <View style={styles.primaryBadge}><Text style={styles.primaryText}>Chính</Text></View>}
            </View>
            <Text style={styles.itemDetail}>{contact.email}</Text>
            <Text style={styles.itemDetail}>{contact.phone}</Text>
            {contact.position && <Text style={styles.itemDetail}>{contact.position}</Text>}
          </View>
        ))
      )}
    </View>
  );

  const renderAddressesTab = () => (
    <View style={styles.tabContent}>
      {addresses.length === 0 ? (
        <View style={styles.emptySection}>
          <Ionicons name="location-outline" size={40} color={Colors.textSecondary} />
          <Text style={styles.emptyText}>Chưa có địa chỉ</Text>
        </View>
      ) : (
        addresses.map((addr) => (
          <View key={addr.id} style={styles.itemCard}>
            <View style={styles.itemHeader}>
              <Text style={styles.itemName}>{addr.street}</Text>
              {addr.isPrimary && <View style={styles.primaryBadge}><Text style={styles.primaryText}>Chính</Text></View>}
            </View>
            <Text style={styles.itemDetail}>{addr.city}{addr.state ? `, ${addr.state}` : ''}</Text>
            <Text style={styles.itemDetail}>{addr.country}{addr.zipCode ? ` - ${addr.zipCode}` : ''}</Text>
            <View style={styles.addressTypeBadge}>
              <Text style={styles.addressTypeText}>{addr.type}</Text>
            </View>
          </View>
        ))
      )}
    </View>
  );

  const renderNotesTab = () => (
    <View style={styles.tabContent}>
      {notes.length === 0 ? (
        <View style={styles.emptySection}>
          <Ionicons name="document-text-outline" size={40} color={Colors.textSecondary} />
          <Text style={styles.emptyText}>Chưa có ghi chú</Text>
        </View>
      ) : (
        notes.map((note) => (
          <View key={note.id} style={styles.noteCard}>
            <View style={styles.noteHeader}>
              <Text style={styles.noteAuthor}>{note.authorName}</Text>
              <Text style={styles.noteTime}>{formatDate(note.createdAt)}</Text>
            </View>
            <Text style={styles.noteContent}>{note.content}</Text>
          </View>
        ))
      )}
    </View>
  );

  const tabs: { key: typeof activeTab; label: string; icon: keyof typeof Ionicons.glyphMap }[] = [
    { key: 'info', label: 'Thông tin', icon: 'information-circle-outline' },
    { key: 'contacts', label: 'Liên hệ', icon: 'people-outline' },
    { key: 'addresses', label: 'Địa chỉ', icon: 'location-outline' },
    { key: 'notes', label: 'Ghi chú', icon: 'document-text-outline' },
  ];

  return (
    <View style={styles.container}>
      <Stack.Screen
        options={{
          headerShown: true,
          headerTitle: customer.name,
          headerBackTitle: 'Quay lại',
          headerStyle: { backgroundColor: Colors.white },
          headerTintColor: Colors.text,
        }}
      />
      <ScrollView contentContainerStyle={styles.scrollContent} stickyHeaderIndices={[1]}>
        <View style={styles.profileHeader}>
          <Ionicons name="person-circle" size={64} color={Colors.primary} />
          <Text style={styles.name}>{customer.name}</Text>
          <View style={[styles.statusBadge, { backgroundColor: statusColors[customer.status] + '20' }]}>
            <View style={[styles.statusDot, { backgroundColor: statusColors[customer.status] }]} />
            <Text style={[styles.statusText, { color: statusColors[customer.status] }]}>
              {statusLabels[customer.status] || customer.status}
            </Text>
          </View>
        </View>

        <View style={styles.tabsRow}>
          {tabs.map((tab) => (
            <TouchableOpacity
              key={tab.key}
              style={[styles.tab, activeTab === tab.key && styles.tabActive]}
              onPress={() => setActiveTab(tab.key)}
              activeOpacity={0.7}
            >
              <Ionicons
                name={tab.icon}
                size={16}
                color={activeTab === tab.key ? Colors.primary : Colors.textSecondary}
              />
              <Text style={[styles.tabLabel, activeTab === tab.key && styles.tabLabelActive]}>
                {tab.label}
              </Text>
            </TouchableOpacity>
          ))}
        </View>

        {activeTab === 'info' && renderInfoTab()}
        {activeTab === 'contacts' && renderContactsTab()}
        {activeTab === 'addresses' && renderAddressesTab()}
        {activeTab === 'notes' && renderNotesTab()}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  scrollContent: {
    flexGrow: 1,
    paddingBottom: 32,
  },
  center: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: Colors.background,
    padding: 24,
  },
  centerText: {
    fontSize: 15,
    color: Colors.textSecondary,
    marginTop: 8,
    textAlign: 'center',
  },
  retryBtn: {
    marginTop: 16,
    paddingHorizontal: 24,
    paddingVertical: 10,
    backgroundColor: Colors.primary,
    borderRadius: 8,
  },
  retryText: {
    color: Colors.white,
    fontWeight: '600',
  },
  profileHeader: {
    alignItems: 'center',
    paddingVertical: 24,
    backgroundColor: Colors.white,
  },
  name: {
    fontSize: 22,
    fontWeight: '700',
    color: Colors.text,
    marginTop: 8,
  },
  statusBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    paddingHorizontal: 12,
    paddingVertical: 4,
    borderRadius: 12,
    marginTop: 8,
  },
  statusDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
  },
  statusText: {
    fontSize: 13,
    fontWeight: '600',
  },
  tabsRow: {
    flexDirection: 'row',
    backgroundColor: Colors.white,
    borderBottomWidth: 1,
    borderBottomColor: Colors.border,
    paddingHorizontal: 8,
  },
  tab: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 4,
    paddingVertical: 12,
    borderBottomWidth: 2,
    borderBottomColor: 'transparent',
  },
  tabActive: {
    borderBottomColor: Colors.primary,
  },
  tabLabel: {
    fontSize: 12,
    color: Colors.textSecondary,
    fontWeight: '500',
  },
  tabLabelActive: {
    color: Colors.primary,
    fontWeight: '600',
  },
  tabContent: {
    padding: 16,
  },
  infoCard: {
    backgroundColor: Colors.card,
    borderRadius: 12,
    padding: 16,
  },
  sectionTitle: {
    fontSize: 15,
    fontWeight: '600',
    color: Colors.text,
    marginBottom: 12,
  },
  infoRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    marginBottom: 10,
  },
  infoLabel: {
    fontSize: 13,
    color: Colors.textSecondary,
    width: 120,
  },
  infoValue: {
    fontSize: 13,
    color: Colors.text,
    flex: 1,
  },
  itemCard: {
    backgroundColor: Colors.card,
    borderRadius: 12,
    padding: 14,
    marginBottom: 8,
  },
  itemHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    marginBottom: 6,
  },
  itemName: {
    fontSize: 15,
    fontWeight: '600',
    color: Colors.text,
    flex: 1,
  },
  primaryBadge: {
    backgroundColor: Colors.primary + '20',
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 4,
  },
  primaryText: {
    fontSize: 11,
    fontWeight: '600',
    color: Colors.primary,
  },
  itemDetail: {
    fontSize: 13,
    color: Colors.textSecondary,
    marginBottom: 2,
  },
  addressTypeBadge: {
    alignSelf: 'flex-start',
    backgroundColor: Colors.background,
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 4,
    marginTop: 4,
  },
  addressTypeText: {
    fontSize: 11,
    color: Colors.textSecondary,
    textTransform: 'capitalize',
  },
  noteCard: {
    backgroundColor: Colors.card,
    borderRadius: 12,
    padding: 14,
    marginBottom: 8,
    borderLeftWidth: 3,
    borderLeftColor: Colors.primary,
  },
  noteHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 6,
  },
  noteAuthor: {
    fontSize: 13,
    fontWeight: '600',
    color: Colors.primary,
  },
  noteTime: {
    fontSize: 11,
    color: Colors.textSecondary,
  },
  noteContent: {
    fontSize: 14,
    color: Colors.text,
    lineHeight: 20,
  },
  emptySection: {
    alignItems: 'center',
    paddingVertical: 40,
    gap: 8,
  },
  emptyText: {
    fontSize: 14,
    color: Colors.textSecondary,
  },
});
