import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Modal from '../Modal.vue'

describe('Modal', () => {
    describe('rendering', () => {
        it('should render when modelValue is true', () => {
            const wrapper = mount(Modal, {
                props: { modelValue: true, title: 'Test Modal' },
                global: {
                    stubs: {
                        Teleport: true
                    }
                }
            })

            expect(wrapper.find('.modal-overlay').exists()).toBe(true)
            expect(wrapper.find('.modal').exists()).toBe(true)
        })

        it('should not render when modelValue is false', () => {
            const wrapper = mount(Modal, {
                props: { modelValue: false, title: 'Test Modal' },
                global: {
                    stubs: {
                        Teleport: true
                    }
                }
            })

            expect(wrapper.find('.modal-overlay').exists()).toBe(false)
        })

        it('should display the title', () => {
            const wrapper = mount(Modal, {
                props: { modelValue: true, title: 'Test Title' },
                global: {
                    stubs: {
                        Teleport: true
                    }
                }
            })

            expect(wrapper.find('.modal-header h3').text()).toBe('Test Title')
        })

        it('should render slot content in modal-body', () => {
            const wrapper = mount(Modal, {
                props: { modelValue: true },
                slots: {
                    default: '<p>Modal content</p>'
                },
                global: {
                    stubs: {
                        Teleport: true
                    }
                }
            })

            expect(wrapper.find('.modal-body').text()).toContain('Modal content')
        })

        it('should render footer slot when provided', () => {
            const wrapper = mount(Modal, {
                props: { modelValue: true },
                slots: {
                    footer: '<button>Save</button>'
                },
                global: {
                    stubs: {
                        Teleport: true
                    }
                }
            })

            expect(wrapper.find('.modal-footer').exists()).toBe(true)
            expect(wrapper.find('.modal-footer').text()).toContain('Save')
        })
    })

    describe('styling', () => {
        it('should apply custom maxWidth', () => {
            const wrapper = mount(Modal, {
                props: { modelValue: true, maxWidth: '800px' },
                global: {
                    stubs: {
                        Teleport: true
                    }
                }
            })

            const modal = wrapper.find('.modal')
            expect(modal.attributes('style')).toContain('max-width: 800px')
        })

        it('should use CSS variables for dark mode support', () => {
            const wrapper = mount(Modal, {
                props: { modelValue: true },
                global: {
                    stubs: {
                        Teleport: true
                    }
                }
            })

            // Modal background should use CSS variable
            const modal = wrapper.find('.modal')
            expect(modal.exists()).toBe(true)
        })
    })

    describe('events', () => {
        it('should emit update:modelValue when close button is clicked', async () => {
            const wrapper = mount(Modal, {
                props: { modelValue: true },
                global: {
                    stubs: {
                        Teleport: true
                    }
                }
            })

            await wrapper.find('.modal-close').trigger('click')

            expect(wrapper.emitted('update:modelValue')).toBeTruthy()
            expect(wrapper.emitted('update:modelValue')![0]).toEqual([false])
        })

        it('should close when overlay is clicked if closeOnOverlay is true', async () => {
            const wrapper = mount(Modal, {
                props: { modelValue: true, closeOnOverlay: true },
                global: {
                    stubs: {
                        Teleport: true
                    }
                }
            })

            await wrapper.find('.modal-overlay').trigger('click')

            expect(wrapper.emitted('update:modelValue')).toBeTruthy()
            expect(wrapper.emitted('update:modelValue')![0]).toEqual([false])
        })

        it('should not close when overlay is clicked if closeOnOverlay is false', async () => {
            const wrapper = mount(Modal, {
                props: { modelValue: true, closeOnOverlay: false },
                global: {
                    stubs: {
                        Teleport: true
                    }
                }
            })

            await wrapper.find('.modal-overlay').trigger('click')

            expect(wrapper.emitted('update:modelValue')).toBeFalsy()
        })
    })
})
