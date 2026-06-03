import { useState, useCallback, useEffect } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, RefreshControl } from 'react-native';
import { useRouter, Stack } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../../../constants/colors';
import ScreenHeader from '../../../components/ScreenHeader';
import SearchBar from '../../../components/SearchBar';
import { SkeletonList } from '../../../components/SkeletonLoader';
import ErrorState from '../../../components/ErrorState';
import EmptyState from '../../../components/EmptyState';
import { knowledgeBaseApi } from '../../../lib/api';
import type { KnowledgeArticle } from '../../../types';

const CATEGORIES = [
  { key: 'all', label: 'Tất cả' },
  { key: 'getting_started', label: 'Bắt đầu' },
  { key: 'troubleshooting', label: 'Xử lý sự cố' },
  { key: 'features', label: 'Tính năng' },
  { key: 'faq', label: 'FAQ' },
];

export default function KnowledgeBaseScreen() {
  const router = useRouter();
  const [articles, setArticles] = useState<KnowledgeArticle[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [refreshing, setRefreshing] = useState(false);
  const [search, setSearch] = useState('');
  const [showSearch, setShowSearch] = useState(false);
  const [category, setCategory] = useState('all');

  const fetchArticles = useCallback(async (isRefresh = false) => {
    if (isRefresh) setRefreshing(true);
    else setLoading(true);
    setError(null);
    try {
      const params: Record<string, any> = {};
      if (category !== 'all') params.category = category;
      if (search) params.search = search;
      const res = await knowledgeBaseApi.getAll(params);
      setArticles(res.data?.data?.content || res.data?.data || res.data || []);
    } catch (err: any) {
      setError(err?.message || 'Không thể tải bài viết');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [category, search]);

  useEffect(() => { fetchArticles(); }, [fetchArticles]);

  const renderItem = useCallback(({ item }: { item: KnowledgeArticle }) => (
    <TouchableOpacity style={styles.articleCard} activeOpacity={0.7}>
      <View style={styles.articleIcon}>
        <Ionicons name="document-text-outline" size={24} color={Colors.primary} />
      </View>
      <View style={styles.articleInfo}>
        <Text style={styles.articleTitle}>{item.title}</Text>
        <Text style={styles.articleMeta} numberOfLines={1}>
          {item.category} • {item.views} lượt xem
        </Text>
        {item.tags.length > 0 && (
          <View style={styles.tagsRow}>
            {item.tags.slice(0, 3).map((tag, i) => (
              <View key={i} style={styles.tag}>
                <Text style={styles.tagText}>{tag}</Text>
              </View>
            ))}
          </View>
        )}
      </View>
      <Ionicons name="chevron-forward" size={18} color={Colors.textSecondary} />
    </TouchableOpacity>
  ), []);

  if (loading && !refreshing) {
    return (
      <View style={styles.container}>
        <Stack.Screen options={{ headerShown: false }} />
        <ScreenHeader title="Kiến thức" showSearch={showSearch} onSearchToggle={() => setShowSearch(!showSearch)} />
        <SkeletonList count={6} />
      </View>
    );
  }

  if (error && articles.length === 0) {
    return (
      <View style={styles.container}>
        <Stack.Screen options={{ headerShown: false }} />
        <ScreenHeader title="Kiến thức" />
        <ErrorState message={error} onRetry={() => fetchArticles()} />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Stack.Screen options={{ headerShown: false }} />
      <ScreenHeader title="Kiến thức" showSearch={showSearch} onSearchToggle={() => setShowSearch(!showSearch)} />

      {showSearch && <SearchBar value={search} onChangeText={setSearch} placeholder="Tìm kiếm bài viết..." />}

      <FlatList
        ListHeaderComponent={() => (
          <FlatList
            horizontal
            data={CATEGORIES}
            keyExtractor={(item) => item.key}
            showsHorizontalScrollIndicator={false}
            contentContainerStyle={styles.categories}
            renderItem={({ item }) => (
              <TouchableOpacity
                style={[styles.categoryChip, category === item.key && styles.categoryChipActive]}
                onPress={() => setCategory(item.key)}
              >
                <Text style={[styles.categoryText, category === item.key && styles.categoryTextActive]}>
                  {item.label}
                </Text>
              </TouchableOpacity>
            )}
          />
        )}
        data={articles}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        contentContainerStyle={[styles.list, articles.length === 0 && styles.listEmpty]}
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={() => fetchArticles(true)} colors={[Colors.primary]} />
        }
        ListEmptyComponent={() => (
          search ? (
            <EmptyState icon="search-outline" title="Không tìm thấy bài viết" subtitle="Thử tìm kiếm với từ khóa khác" />
          ) : (
            <EmptyState icon="book-outline" title="Chưa có bài viết" subtitle="Các bài viết sẽ hiển thị ở đây" />
          )
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  list: { padding: 16, paddingBottom: 100 },
  listEmpty: { flexGrow: 1 },
  categories: {
    paddingHorizontal: 16,
    paddingVertical: 12,
    gap: 8,
  },
  categoryChip: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
    backgroundColor: Colors.background,
    marginRight: 8,
  },
  categoryChipActive: {
    backgroundColor: Colors.primary,
  },
  categoryText: {
    fontSize: 13,
    color: Colors.textSecondary,
    fontWeight: '500',
  },
  categoryTextActive: {
    color: Colors.white,
  },
  articleCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: Colors.card,
    padding: 14,
    borderRadius: 12,
    marginBottom: 8,
    gap: 12,
    shadowColor: '#000',
    shadowOpacity: 0.03,
    shadowOffset: { width: 0, height: 2 },
    shadowRadius: 4,
    elevation: 2,
  },
  articleIcon: {
    width: 44,
    height: 44,
    borderRadius: 12,
    backgroundColor: Colors.primary + '15',
    justifyContent: 'center',
    alignItems: 'center',
  },
  articleInfo: { flex: 1 },
  articleTitle: { fontSize: 15, fontWeight: '600', color: Colors.text, marginBottom: 2 },
  articleMeta: { fontSize: 12, color: Colors.textSecondary },
  tagsRow: { flexDirection: 'row', gap: 4, marginTop: 4 },
  tag: {
    backgroundColor: Colors.background,
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 4,
  },
  tagText: { fontSize: 10, color: Colors.textSecondary },
});
