import { pgTable, serial, text, integer, doublePrecision, timestamp, date } from 'drizzle-orm/pg-core'

export const items = pgTable('items', {
  id: serial('id').primaryKey(),
  name: text('name').notNull().unique(),
  category: text('category').notNull(), // 'food' | 'drink'
  useCount: integer('use_count').notNull().default(0),
  lastUsedAt: timestamp('last_used_at'),
})

export const tags = pgTable('tags', {
  id: serial('id').primaryKey(),
  name: text('name').notNull().unique(),
  useCount: integer('use_count').notNull().default(0),
  lastUsedAt: timestamp('last_used_at'),
})

export const entries = pgTable('entries', {
  id: serial('id').primaryKey(),
  timestamp: timestamp('timestamp').notNull(),
  type: text('type').notNull(), // 'food' | 'drink'
  itemId: integer('item_id').references(() => items.id),
  label: text('label'), // set for a one-off (itemId null); exactly one of itemId/label is set
})

export const dailyLogs = pgTable('daily_logs', {
  date: date('date').primaryKey(), // 'YYYY-MM-DD'
  medicationTakenAt: timestamp('medication_taken_at'),
  wellbeingRating: doublePrecision('wellbeing_rating'), // 1-10 in 0.5 steps (1 = Super Bad, 10 = Perfect Day)
  wellbeingLoggedAt: timestamp('wellbeing_logged_at'),
  digestionRating: doublePrecision('digestion_rating'), // 1-5 in 0.5 steps
  digestionLoggedAt: timestamp('digestion_logged_at'),
  contextTagId: integer('context_tag_id').references(() => tags.id),
  notes: text('notes'),
})
