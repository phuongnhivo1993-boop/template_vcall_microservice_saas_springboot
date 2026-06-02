'use client';

import { Skeleton, Card, Row, Col } from 'antd';

interface LoadingSkeletonProps {
  type?: 'table' | 'card' | 'detail' | 'stats';
  rows?: number;
}

export default function LoadingSkeleton({ type = 'table', rows = 5 }: LoadingSkeletonProps) {
  if (type === 'stats') {
    return (
      <Row gutter={[16, 16]}>
        {[1, 2, 3, 4].map(i => (
          <Col xs={24} sm={12} lg={6} key={i}>
            <Card><Skeleton active paragraph={{ rows: 1 }} /></Card>
          </Col>
        ))}
      </Row>
    );
  }

  if (type === 'detail') {
    return (
      <Card>
        <Skeleton active avatar paragraph={{ rows: 3 }} />
        <div style={{ marginTop: 24 }}>
          <Skeleton active paragraph={{ rows: 6 }} />
        </div>
      </Card>
    );
  }

  if (type === 'card') {
    return (
      <Row gutter={[16, 16]}>
        {[1, 2, 3].map(i => (
          <Col xs={24} sm={12} lg={8} key={i}>
            <Card cover={<Skeleton.Image style={{ width: '100%', height: 160 }} />}>
              <Skeleton active paragraph={{ rows: 2 }} />
            </Card>
          </Col>
        ))}
      </Row>
    );
  }

  return (
    <Card>
      <Skeleton active paragraph={{ rows: 1 }} style={{ marginBottom: 16 }} />
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} style={{ padding: '12px 0', borderBottom: i < rows - 1 ? '1px solid #f0f0f0' : 'none' }}>
          <Skeleton active paragraph={{ rows: 1 }} title={false} />
        </div>
      ))}
    </Card>
  );
}
